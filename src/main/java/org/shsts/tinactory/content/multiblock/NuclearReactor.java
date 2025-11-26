package org.shsts.tinactory.content.multiblock;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllMenus;
import org.shsts.tinactory.content.machine.Boiler;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.logistics.WrapperItemHandler;
import org.shsts.tinactory.core.multiblock.Multiblock;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;

import java.util.Arrays;

import static org.shsts.tinactory.content.AllCapabilities.PROCESSOR;
import static org.shsts.tinactory.core.util.CodecHelper.parseIntArray;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NuclearReactor extends Multiblock implements INBTSerializable<CompoundTag>, IProcessor {
    private final Properties properties;
    private final Boiler boiler;
    private final WrapperItemHandler reactorItems;

    private int rows;
    private int columns;

    public record Properties(double baseHeat, double baseDecay,
        int minHeight, int[] rows, int[] columns) {
        public static Properties fromJson(JsonObject jo) {
            return new Properties(
                GsonHelper.getAsDouble(jo, "baseHeat"),
                GsonHelper.getAsDouble(jo, "baseDecay"),
                GsonHelper.getAsInt(jo, "minHeight"),
                parseIntArray(GsonHelper.getAsJsonArray(jo, "rows")),
                parseIntArray(GsonHelper.getAsJsonArray(jo, "columns")));
        }
    }

    public NuclearReactor(BlockEntity blockEntity, Builder<?> builder,
        Properties properties) {
        super(blockEntity, builder);
        this.properties = properties;
        this.boiler = new Boiler(properties.baseHeat, properties.baseDecay);

        var maxRows = Arrays.stream(properties.rows).max().orElseThrow();
        var maxColumns = Arrays.stream(properties.columns).max().orElseThrow();
        this.reactorItems = new WrapperItemHandler(maxRows * maxColumns);
        reactorItems.onUpdate(blockEntity::setChanged);
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

    public double getHeat() {
        return boiler.getHeat();
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
    public IMenuType menu(IMachine machine) {
        return machine instanceof DigitalInterface ?
            AllMenus.NUCLEAR_REACTOR_DIGITAL_INTERFACE : AllMenus.NUCLEAR_REACTOR;
    }

    @Override
    public void onPreWork() {}

    @Override
    public void onWorkTick(double partial) {

    }

    @Override
    public double getProgress() {
        return 0;
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
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        boiler.deserializeNBT(tag.getCompound("boiler"));
        StackHelper.deserializeItemHandler(reactorItems, tag.getCompound("reactorItems"));
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
