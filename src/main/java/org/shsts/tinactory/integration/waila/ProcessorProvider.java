package org.shsts.tinactory.integration.waila;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.api.ui.IElementHelper;
import mcp.mobius.waila.api.ui.IProgressStyle;
import mcp.mobius.waila.impl.ui.ProgressStyle;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.machine.IMachineProcessor;
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
import org.shsts.tinactory.core.util.I18n;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.AllCapabilities.PROCESSOR;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.util.ClientUtil.NUMBER_FORMAT;
import static org.shsts.tinactory.core.util.ClientUtil.PERCENTAGE_FORMAT;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessorProvider implements IComponentProvider, IServerDataProvider<BlockEntity> {
    public static final ProcessorProvider INSTANCE = new ProcessorProvider();

    private static final ResourceLocation TAG = modLoc("processor");
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int PROGRESS_COLOR = 0xFF00B1B4;
    private static final int HEAT_COLOR = 0xFFD41200;
    private static final int CLEANNESS_COLOR = 0xFF00D412;
    private static final int POWER_COLOR = 0xFFD4CD00;
    private static final NumberFormat PROGRESS_FORMAT = new DecimalFormat("0.0");
    private static final Vec2 ITEM_SIZE = new Vec2(10f, 10f);
    private static final Vec2 FLUID_SIZE = new Vec2(8f, 8f);
    private static final Vec2 ITEM_OFFSET = new Vec2(0f, -1f);

    private static TranslatableComponent tr(String id, Object... args) {
        return I18n.tr("tinactory.tooltip." + id, args);
    }

    private boolean hasSpace;
    private IElementHelper helper;
    private ITooltip tooltip;

    private void appendSpace() {
        if (!hasSpace) {
            tooltip.add(helper.spacer(0, SPACING).tag(TAG));
            hasSpace = true;
        }
    }

    private void add(IElement element) {
        appendSpace();
        tooltip.add(element.tag(TAG));
    }

    private void add(List<IElement> elements) {
        appendSpace();
        for (var element : elements) {
            element.tag(TAG);
        }
        tooltip.add(elements);
    }

    private IProgressStyle progressStyle(int color) {
        var ret = (ProgressStyle) helper.progressStyle().color(color).textColor(TEXT_COLOR);
        ret.shadow = true;
        return ret;
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
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var tag = accessor.getServerData();
        this.tooltip = tooltip;
        helper = tooltip.getElementHelper();
        hasSpace = false;
        var borderStyle = helper.borderStyle();

        if (tag.contains("tinactoryHeat", Tag.TAG_DOUBLE)) {
            var heat = tag.getDouble("tinactoryHeat");
            var maxHeat = tag.getDouble("tinactoryMaxHeat");
            var text = tr("heat", NUMBER_FORMAT.format(heat));
            add(helper.progress((float) (heat / maxHeat), text,
                progressStyle(HEAT_COLOR), borderStyle));
        }

        if (tag.contains("tinactoryBatteryPower", Tag.TAG_LONG)) {
            var power = tag.getLong("tinactoryBatteryPower");
            var capacity = tag.getLong("tinactoryBatteryCapacity");
            var text = tr("power", ClientUtil.getNumberString(power),
                ClientUtil.getNumberString(capacity));
            add(helper.progress((float) power / (float) capacity, text,
                progressStyle(POWER_COLOR), borderStyle));
        }

        if (tag.contains("tinactoryCleanness", Tag.TAG_DOUBLE)) {
            var cleanness = tag.getDouble("tinactoryCleanness");
            var text = tr("cleanness", PERCENTAGE_FORMAT.format(cleanness));
            add(helper.progress((float) cleanness, text,
                progressStyle(CLEANNESS_COLOR), borderStyle));
        }

        if (tag.contains("tinactoryProgress", Tag.TAG_LONG)) {
            var progress = (double) tag.getLong("tinactoryProgress") / 20d;
            var maxProgress = (double) tag.getLong("tinactoryMaxProgress") / 20d;
            var format = maxProgress < 10d ? PROGRESS_FORMAT : NUMBER_FORMAT;
            var text = tr("progress", format.format(progress), format.format(maxProgress));
            add(helper.progress((float) (progress / maxProgress), text,
                progressStyle(PROGRESS_COLOR), borderStyle));
        }

        hasSpace = false;

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

        if (tag.contains("tinactoryWorkSpeed", Tag.TAG_DOUBLE)) {
            var workSpeed = tag.getDouble("tinactoryWorkSpeed");
            var text = helper.text(tr("workSpeed", PERCENTAGE_FORMAT.format(workSpeed)));
            add(text);
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, ServerPlayer player, Level world,
        BlockEntity blockEntity, boolean showDetails) {
        var cap = PROCESSOR.tryGet(blockEntity);
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
            tag.putLong("tinactoryBatteryPower", battery.powerLevel());
            tag.putLong("tinactoryBatteryCapacity", battery.powerCapacity());
        }

        if (processor instanceof Cleanroom cleanroom) {
            tag.putDouble("tinactoryCleanness", cleanroom.getProgress());
        }
    }
}
