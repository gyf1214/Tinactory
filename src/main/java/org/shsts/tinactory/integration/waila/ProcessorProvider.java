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
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec2;
import org.shsts.tinactory.api.machine.IMachineProcessor;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.content.electric.IBatteryBox;
import org.shsts.tinactory.content.machine.IBoiler;
import org.shsts.tinactory.content.multiblock.Cleanroom;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingResults;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.core.util.I18n;

import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.AllCapabilities.PROCESSOR;
import static org.shsts.tinactory.core.util.ClientUtil.NUMBER_FORMAT;
import static org.shsts.tinactory.core.util.ClientUtil.PERCENTAGE_FORMAT;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessorProvider implements IComponentProvider, IServerDataProvider<BlockEntity> {
    public static final ProcessorProvider INSTANCE = new ProcessorProvider();

    private static final ResourceLocation TAG = modLoc("processor");
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int PROGRESS_COLOR = 0xFF0000FF;
    private static final int HEAT_COLOR = 0xFFFF0000;
    private static final int CLEANNESS_COLOR = 0xFF00FF00;
    private static final int POWER_COLOR = 0xFFFFFF00;

    private static TranslatableComponent tr(String id, Object... args) {
        return I18n.tr("tinactory.tooltip." + id, args);
    }

    private void add(ITooltip tooltip, IElement element) {
        tooltip.add(element.tag(TAG));
    }

    private void add(ITooltip tooltip, List<IElement> elements) {
        tooltip.add(elements);
    }

    private IProgressStyle progressStyle(IElementHelper helper, int color) {
        var ret = (ProgressStyle) helper.progressStyle().color(color).textColor(TEXT_COLOR);
        ret.shadow = true;
        return ret;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var tag = accessor.getServerData();
        var helper = tooltip.getElementHelper();
        var borderStyle = helper.borderStyle();

        if (tag.contains("tinactoryHeat", Tag.TAG_DOUBLE)) {
            var heat = tag.getDouble("tinactoryHeat");
            var maxHeat = tag.getDouble("tinactoryMaxHeat");
            var text = tr("heat", NUMBER_FORMAT.format(heat));
            add(tooltip, helper.progress((float) (heat / maxHeat), text,
                progressStyle(helper, HEAT_COLOR), borderStyle));
        }

        if (tag.contains("tinactoryBatteryPower", Tag.TAG_LONG)) {
            var power = tag.getLong("tinactoryBatteryPower");
            var capacity = tag.getLong("tinactoryBatteryCapacity");
            var text = tr("power", ClientUtil.getNumberString(power),
                ClientUtil.getNumberString(capacity));
            add(tooltip, helper.progress((float) power / (float) capacity, text,
                progressStyle(helper, POWER_COLOR), borderStyle));
        }

        if (tag.contains("tinactoryCleanness", Tag.TAG_DOUBLE)) {
            var cleanness = tag.getDouble("tinactoryCleanness");
            var text = new TextComponent(PERCENTAGE_FORMAT.format(cleanness));
            add(tooltip, helper.progress((float) cleanness, text,
                progressStyle(helper, CLEANNESS_COLOR), borderStyle));
        }

        if (tag.contains("tinactoryProgress", Tag.TAG_LONG)) {
            var progress = tag.getLong("tinactoryProgress");
            var maxProgress = tag.getLong("tinactoryMaxProgress");
            var text = tr("progress", NUMBER_FORMAT.format(progress / 20),
                NUMBER_FORMAT.format(maxProgress / 20));
            add(tooltip, helper.progress((float) progress / (float) maxProgress, text,
                progressStyle(helper, PROGRESS_COLOR), borderStyle));
        }

        if (tag.contains("tinactoryInputs", Tag.TAG_LIST)) {
            var listTag = tag.getList("tinactoryInputs", Tag.TAG_COMPOUND);
            var line = new ArrayList<IElement>();
            line.add(helper.text(tr("inputs")).translate(new Vec2(0, 3.5f)).tag(TAG));
            for (var tag1 : listTag) {
                var input = CodecHelper.parseTag(ProcessingIngredients.CODEC, tag1);
                if (input instanceof ProcessingIngredients.ItemIngredient item) {
                    line.add(helper.item(item.stack()).tag(TAG));
                } else if (input instanceof ProcessingIngredients.FluidIngredient fluid) {
                    line.add(helper.fluid(fluid.fluid()).tag(TAG));
                }
            }
            if (line.size() > 1) {
                add(tooltip, line);
            }
        }

        if (tag.contains("tinactoryOutputs", Tag.TAG_LIST)) {
            var listTag = tag.getList("tinactoryOutputs", Tag.TAG_COMPOUND);
            var line = new ArrayList<IElement>();
            line.add(helper.text(tr("outputs")).translate(new Vec2(0, 3.5f)).tag(TAG));
            for (var tag1 : listTag) {
                var output = CodecHelper.parseTag(ProcessingResults.CODEC, tag1);
                if (output instanceof ProcessingResults.ItemResult item) {
                    line.add(helper.item(item.stack).tag(TAG));
                } else if (output instanceof ProcessingResults.FluidResult fluid) {
                    line.add(helper.fluid(fluid.stack).tag(TAG));
                }
            }
            if (line.size() > 1) {
                add(tooltip, line);
            }
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
