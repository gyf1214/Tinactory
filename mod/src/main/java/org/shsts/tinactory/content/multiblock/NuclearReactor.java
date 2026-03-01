package org.shsts.tinactory.content.multiblock;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.shsts.tinactory.AllMenus;
import org.shsts.tinactory.AllTags;
import org.shsts.tinactory.api.logistics.ContainerAccess;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.content.machine.Boiler;
import org.shsts.tinactory.content.machine.IBoiler;
import org.shsts.tinactory.content.tool.INuclearItem;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.integration.logistics.WrapperItemHandler;
import org.shsts.tinactory.core.metrics.MetricsManager;
import org.shsts.tinactory.core.multiblock.Multiblock;
import org.shsts.tinactory.core.multiblock.MultiblockInterface;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.AllCapabilities.PROCESSOR;
import static org.shsts.tinactory.core.util.CodecHelper.parseIntArray;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NuclearReactor extends Multiblock implements IBoiler,
    INBTSerializable<CompoundTag> {
    private static final int[] NEIGHBOR_X = new int[]{-1, 1, 0, 0};
    private static final int[] NEIGHBOR_Y = new int[]{0, 0, -1, 1};

    private class Cell implements INuclearCell {
        private double fastNeutron = 0d;
        private double slowNeutron = 0d;
        private double oldFastNeutron = 0d;
        private double oldSlowNeutron = 0d;

        public void clampSave() {
            fastNeutron = Math.max(0d, fastNeutron);
            slowNeutron = Math.max(0d, slowNeutron);
            oldFastNeutron = fastNeutron;
            oldSlowNeutron = slowNeutron;
        }

        @Override
        public double getFastNeutron() {
            return fastNeutron;
        }

        @Override
        public double getSlowNeutron() {
            return slowNeutron;
        }

        @Override
        public double getHeat() {
            return boiler.heat();
        }

        @Override
        public void incFastNeutron(double val) {
            fastNeutron += val;
        }

        @Override
        public void incSlowNeutron(double val) {
            slowNeutron += val;
        }

        @Override
        public void incHeat(double val) {
            heatInc += val;
        }

        @Override
        public void incReaction(double val) {
            reactions += val;
        }
    }

    private final Properties properties;
    private final Boiler boiler;
    private final WrapperItemHandler reactorItems;
    private final Cell[] cells;

    private int rows;
    private int columns;
    private double heatInc;
    private double reactions;

    public record Properties(double baseHeat, double baseDecay,
        double minHeat, double maxHeat,
        int minHeight, int[] rows, int[] columns,
        double fastDiffusion, double slowDiffusionRate, double maxSlowDiffusion,
        double fastReflection, double slowReflection,
        double boilerParallel) {
        public static Properties fromJson(JsonObject jo) {
            return new Properties(
                GsonHelper.getAsDouble(jo, "baseHeat"),
                GsonHelper.getAsDouble(jo, "baseDecay"),
                GsonHelper.getAsDouble(jo, "minHeat"),
                GsonHelper.getAsDouble(jo, "maxHeat"),
                GsonHelper.getAsInt(jo, "minHeight"),
                parseIntArray(GsonHelper.getAsJsonArray(jo, "rows")),
                parseIntArray(GsonHelper.getAsJsonArray(jo, "columns")),
                GsonHelper.getAsDouble(jo, "fastDiffusion"),
                GsonHelper.getAsDouble(jo, "slowDiffusionRate"),
                GsonHelper.getAsDouble(jo, "maxSlowDiffusion"),
                GsonHelper.getAsDouble(jo, "fastReflection"),
                GsonHelper.getAsDouble(jo, "slowReflection"),
                GsonHelper.getAsDouble(jo, "boilerParallel"));
        }
    }

    public NuclearReactor(BlockEntity blockEntity, Builder<?> builder,
        Properties properties) {
        super(blockEntity, builder);
        this.properties = properties;
        this.boiler = new Boiler(properties.baseHeat, properties.baseDecay);

        var maxRows = Arrays.stream(properties.rows).max().orElseThrow();
        var maxColumns = Arrays.stream(properties.columns).max().orElseThrow();
        var maxCells = maxRows * maxColumns;
        this.reactorItems = new WrapperItemHandler(maxCells);
        reactorItems.onUpdate(blockEntity::setChanged);
        this.cells = new Cell[maxCells];
        for (var i = 0; i < maxCells; i++) {
            reactorItems.setFilter(i, stack -> stack.is(AllTags.NUCLEAR_ITEM));
            cells[i] = new Cell();
        }
    }

    public int rows() {
        return rows;
    }

    public int columns() {
        return columns;
    }

    public IItemHandler reactorItems() {
        return reactorItems;
    }

    @Override
    protected void doCheckMultiblock(CheckContext ctx) {
        super.doCheckMultiblock(ctx);
        if (!ctx.isFailed()) {
            var i = (int) ctx.getProperty("height") - properties.minHeight;
            rows = properties.rows[i];
            columns = properties.columns[i];
        }
    }

    @Override
    public IMenuType menu(MultiblockInterface machine) {
        return machine.isDigital() ? AllMenus.NUCLEAR_REACTOR_DIGITAL_INTERFACE :
            AllMenus.NUCLEAR_REACTOR;
    }

    @Override
    public void onContainerReady() {
        if (multiblockInterface != null) {
            var container = multiblockInterface.container().orElseThrow();
            boiler.setContainer(
                container.getPort(1, ContainerAccess.INTERNAL).asFluid(),
                container.getPort(2, ContainerAccess.INTERNAL).asFluid());
        }
    }

    @Override
    protected void onInvalidate() {
        super.onInvalidate();
        boiler.resetContainer();
    }

    @Override
    public void onPreWork() {}

    @Override
    public void onWorkTick(double partial) {
        if (multiblockInterface == null) {
            return;
        }

        heatInc = 0d;
        reactions = 0d;

        var size = rows * columns;
        for (var i = 0; i < size; i++) {
            var stack = reactorItems.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof INuclearItem item) {
                var stack1 = item.tickCell(stack, cells[i]);
                reactorItems.setStackInSlot(i, stack1);
            }
        }

        for (var i = 0; i < size; i++) {
            cells[i].clampSave();
        }

        var slowDiffusion = Math.min(properties.maxSlowDiffusion,
            properties.slowDiffusionRate * boiler.heat());
        for (var y = 0; y < rows; y++) {
            for (var x = 0; x < columns; x++) {
                var i = y * columns + x;
                var diffFast = cells[i].oldFastNeutron * properties.fastDiffusion;
                var diffSlow = cells[i].oldSlowNeutron * slowDiffusion;
                cells[i].fastNeutron -= diffFast;
                cells[i].slowNeutron -= diffSlow;
                diffFast /= 4d;
                diffSlow /= 4d;

                for (var k = 0; k < 4; k++) {
                    var x1 = x + NEIGHBOR_X[k];
                    var y1 = y + NEIGHBOR_Y[k];
                    if (x1 >= 0 && x1 < columns && y1 >= 0 && y1 < rows) {
                        var i1 = y1 * columns + x1;
                        cells[i1].fastNeutron += diffFast;
                        cells[i1].slowNeutron += diffSlow;
                    } else {
                        cells[i].fastNeutron += diffFast * properties.fastReflection;
                        cells[i].slowNeutron += diffSlow * properties.slowReflection;
                    }
                }
            }
        }

        var world = blockEntity.getLevel();
        assert world != null;
        boiler.tick(world, heatInc, properties.boilerParallel, (input, output) -> {
            MetricsManager.reportFluid("fluid_consumed", multiblockInterface, input);
            MetricsManager.reportFluid("fluid_produced", multiblockInterface, output);
        });

        blockEntity.setChanged();
    }

    @Override
    public double heat() {
        return boiler.heat();
    }

    @Override
    public double minHeat() {
        return properties.minHeat;
    }

    @Override
    public double maxHeat() {
        return properties.maxHeat;
    }

    @Override
    public long progressTicks() {
        return MathUtil.compare(reactions) > 0 ? 1 : 0;
    }

    @Override
    public boolean isWorking(double partial) {
        return MathUtil.compare(reactions) > 0;
    }

    @Override
    public long maxProgressTicks() {
        return 0;
    }

    @Override
    public double workSpeed() {
        return -1d;
    }

    @Override
    public Optional<IProcessingObject> getInfo(int port, int index) {
        if (index > 0) {
            return Optional.empty();
        }
        return switch (port) {
            case 1 -> boiler.inputInfo();
            case 2 -> boiler.outputInfo();
            default -> Optional.empty();
        };
    }

    @Override
    public List<IProcessingObject> getAllInfo() {
        var ret = new ArrayList<IProcessingObject>();
        boiler.addAllInfo(ret::add);
        return ret;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == PROCESSOR.get()) {
            return myself();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.put("boiler", boiler.serializeNBT());
        tag.put("reactorItems", StackHelper.serializeItemHandler(reactorItems));

        var listTag = new ListTag();
        for (var cell : cells) {
            var tag1 = new CompoundTag();
            tag1.putDouble("fast", cell.fastNeutron);
            tag1.putDouble("slow", cell.slowNeutron);
            listTag.add(tag1);
        }
        tag.put("cells", listTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        boiler.deserializeNBT(tag.getCompound("boiler"));
        StackHelper.deserializeItemHandler(reactorItems, tag.getCompound("reactorItems"));

        var i = 0;
        for (var tag1 : tag.getList("cells", Tag.TAG_COMPOUND)) {
            var tag2 = (CompoundTag) tag1;
            var cell = cells[i++];
            cell.fastNeutron = tag2.getDouble("fast");
            cell.slowNeutron = tag2.getDouble("slow");
        }
    }

    @Override
    public CompoundTag serializeOnUpdate() {
        var tag = super.serializeOnUpdate();
        if (multiblockInterface != null) {
            tag.putInt("rows", rows);
            tag.putInt("columns", columns);
        }
        return tag;
    }

    @Override
    public void deserializeOnUpdate(CompoundTag tag) {
        super.deserializeOnUpdate(tag);
        if (tag.contains("rows", Tag.TAG_INT)) {
            rows = tag.getInt("rows");
            columns = tag.getInt("columns");
        }
    }
}
