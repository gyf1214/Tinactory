package org.shsts.tinactory.integration.waila;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElement;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineProcessor;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.content.electric.IBatteryBox;
import org.shsts.tinactory.content.machine.IBoiler;
import org.shsts.tinactory.content.multiblock.Cleanroom;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingResults;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinactory.core.util.CodecHelper;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllCapabilities.PROCESSOR;
import static org.shsts.tinactory.core.util.ClientUtil.NUMBER_FORMAT;
import static org.shsts.tinactory.core.util.ClientUtil.PERCENTAGE_FORMAT;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.integration.waila.Waila.CLEANNESS;
import static org.shsts.tinactory.integration.waila.Waila.HEAT;
import static org.shsts.tinactory.integration.waila.Waila.POWER;
import static org.shsts.tinactory.integration.waila.Waila.PROGRESS;
import static org.shsts.tinactory.integration.waila.Waila.RECIPE;
import static org.shsts.tinactory.integration.waila.Waila.WORK_SPEED;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessorProvider extends ProviderBase implements IComponentProvider,
    IServerDataProvider<BlockEntity> {
    public static final ProcessorProvider INSTANCE = new ProcessorProvider();

    private static final int PROGRESS_COLOR = 0xFF00B1B4;
    private static final int HEAT_COLOR = 0xFFD41200;
    private static final int CLEANNESS_COLOR = 0xFF00D412;
    private static final int POWER_COLOR = 0xFFD4CD00;
    private static final NumberFormat PROGRESS_FORMAT = new DecimalFormat("0.0");
    private static final Vec2 ITEM_SIZE = new Vec2(10f, 10f);
    private static final Vec2 FLUID_SIZE = new Vec2(8f, 8f);
    private static final Vec2 ITEM_OFFSET = new Vec2(0f, -1f);

    public ProcessorProvider() {
        super(modLoc("processor"));
    }

    private void itemElement(List<IElement> line, ItemStack stack) {
        var stack1 = StackHelper.copyWithCount(stack, 1);
        line.add(helper.item(stack1, 0.5f).size(ITEM_SIZE).translate(ITEM_OFFSET));
    }

    private void fluidElement(List<IElement> line, FluidStack stack) {
        line.add(helper.spacer(1, 0));
        line.add(helper.fluid(stack).size(FLUID_SIZE));
        line.add(helper.spacer(1, 0));
    }

    @Override
    protected void doAppendTooltip(CompoundTag tag, BlockAccessor accessor, IPluginConfig config) {
        if (config.get(HEAT) && tag.contains("tinactoryHeat", Tag.TAG_DOUBLE)) {
            var heat = tag.getDouble("tinactoryHeat");
            var maxHeat = tag.getDouble("tinactoryMaxHeat");
            var text = tr("heat", NUMBER_FORMAT.format(heat));
            addProgress((float) (heat / maxHeat), text, HEAT_COLOR);
        }

        if (config.get(POWER) && tag.contains("tinactoryPower", Tag.TAG_LONG)) {
            var power = tag.getLong("tinactoryPower");
            var capacity = tag.getLong("tinactoryPowerCapacity");
            var text = tr("power", ClientUtil.getNumberString(power),
                ClientUtil.getNumberString(capacity));
            addProgress((float) power / (float) capacity, text, POWER_COLOR);
        }

        if (config.get(CLEANNESS) && tag.contains("tinactoryCleanness", Tag.TAG_DOUBLE)) {
            var cleanness = tag.getDouble("tinactoryCleanness");
            var text = tr("cleanness", PERCENTAGE_FORMAT.format(cleanness));
            addProgress((float) cleanness, text, CLEANNESS_COLOR);
        }

        if (config.get(PROGRESS) && tag.contains("tinactoryProgress", Tag.TAG_LONG)) {
            var progress = (double) tag.getLong("tinactoryProgress") / 20d;
            var maxProgress = (double) tag.getLong("tinactoryMaxProgress") / 20d;
            var format = maxProgress < 10d ? PROGRESS_FORMAT : NUMBER_FORMAT;
            var text = tr("progress", format.format(progress), format.format(maxProgress));
            addProgress((float) (progress / maxProgress), text, PROGRESS_COLOR);
        }

        newSpace();

        if (config.get(RECIPE)) {
            if (tag.contains("tinactoryInputs", Tag.TAG_LIST)) {
                var listTag = tag.getList("tinactoryInputs", Tag.TAG_COMPOUND);
                var line = new ArrayList<IElement>();
                line.add(helper.text(tr("inputs")));
                for (var tag1 : listTag) {
                    var input = CodecHelper.parseTag(ProcessingIngredients.CODEC, tag1);
                    if (input instanceof ProcessingIngredients.ItemIngredient item) {
                        itemElement(line, item.stack());
                    } else if (input instanceof ProcessingIngredients.FluidIngredient fluid) {
                        fluidElement(line, fluid.fluid());
                    }
                }
                if (line.size() > 1) {
                    add(line);
                }
            }

            if (tag.contains("tinactoryOutputs", Tag.TAG_LIST)) {
                var listTag = tag.getList("tinactoryOutputs", Tag.TAG_COMPOUND);
                var line = new ArrayList<IElement>();
                line.add(helper.text(tr("outputs")));
                for (var tag1 : listTag) {
                    var output = CodecHelper.parseTag(ProcessingResults.CODEC, tag1);
                    if (output instanceof ProcessingResults.ItemResult item) {
                        itemElement(line, item.stack);
                    } else if (output instanceof ProcessingResults.FluidResult fluid) {
                        fluidElement(line, fluid.stack);
                    }
                }
                if (line.size() > 1) {
                    add(line);
                }
            }
        }

        if (config.get(WORK_SPEED) && tag.contains("tinactoryWorkSpeed", Tag.TAG_DOUBLE)) {
            var workSpeed = tag.getDouble("tinactoryWorkSpeed");
            var text = helper.text(tr("workSpeed", PERCENTAGE_FORMAT.format(workSpeed)));
            add(text);
        }
    }

    private Optional<IProcessor> getProcessor(BlockEntity blockEntity) {
        return PROCESSOR.tryGet(blockEntity)
            .or(() -> MACHINE.tryGet(blockEntity).flatMap(IMachine::processor));
    }

    @Override
    public void appendServerData(CompoundTag tag, ServerPlayer player, Level world,
        BlockEntity blockEntity, boolean showDetails) {
        var cap = getProcessor(blockEntity);
        if (cap.isEmpty()) {
            return;
        }

        var processor = cap.get();

        if (processor instanceof IMachineProcessor machine) {
            var inputs = new ListTag();
            var outputs = new ListTag();
            for (var info : machine.getAllInfo()) {
                if (info instanceof IProcessingIngredient ingredient) {
                    inputs.add(CodecHelper.encodeTag(ProcessingIngredients.CODEC, ingredient));
                } else if (info instanceof IProcessingResult result) {
                    outputs.add(CodecHelper.encodeTag(ProcessingResults.CODEC, result));
                }
            }
            tag.put("tinactoryInputs", inputs);
            tag.put("tinactoryOutputs", outputs);

            var maxProgress = machine.maxProgressTicks();
            if (maxProgress > 0) {
                var progress = machine.progressTicks();
                tag.putLong("tinactoryProgress", machine instanceof IBoiler ?
                    maxProgress - progress : progress);
                tag.putLong("tinactoryMaxProgress", maxProgress);
            }

            var workSpeed = machine.workSpeed();
            if (workSpeed >= 0) {
                tag.putDouble("tinactoryWorkSpeed", workSpeed);
            }

            if (processor instanceof IBoiler boiler) {
                tag.putDouble("tinactoryHeat", boiler.heat());
                tag.putDouble("tinactoryMaxHeat", boiler.maxHeat());
            }
        }

        if (processor instanceof IBatteryBox battery) {
            tag.putLong("tinactoryPower", battery.powerLevel());
            tag.putLong("tinactoryPowerCapacity", battery.powerCapacity());
        }

        if (processor instanceof Cleanroom cleanroom) {
            tag.putDouble("tinactoryCleanness", cleanroom.getProgress());
        }
    }
}
