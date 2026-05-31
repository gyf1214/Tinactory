package org.shsts.tinactory.compat.waila;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElement;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.content.autocraft.MECraftCpu;
import org.shsts.tinactory.core.autocraft.api.ExecutionError;
import org.shsts.tinactory.core.autocraft.api.JobState;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.integration.gui.client.FluidRenderDescriptor;
import org.shsts.tinactory.integration.gui.client.ItemRenderDescriptor;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinactory.integration.util.ClientUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.compat.waila.Waila.ME_CRAFT_CPU;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.integration.common.CapabilityProvider.tryGetProvider;
import static org.shsts.tinactory.integration.util.ClientUtil.NUMBER_FORMAT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MECraftCpuProvider extends ProviderBase implements IServerDataProvider<BlockEntity> {
    public static final MECraftCpuProvider INSTANCE = new MECraftCpuProvider();

    private static final String PREFIX = "tinactoryMECraftCpu";
    private static final String STATE_KEY = PREFIX + "State";
    private static final String TARGET_KEY = PREFIX + "Target";
    private static final String TARGET_AMOUNT_KEY = PREFIX + "TargetAmount";
    private static final String COMPLETED_STEPS_KEY = PREFIX + "CompletedSteps";
    private static final String TOTAL_STEPS_KEY = PREFIX + "TotalSteps";
    private static final String ERROR_KEY = PREFIX + "Error";
    private static final String MEMORY_LIMIT_KEY = PREFIX + "MemoryLimit";
    private static final String MEMORY_USAGE_KEY = PREFIX + "MemoryUsage";

    public MECraftCpuProvider() {
        super(modLoc("me_craft_cpu"));
    }

    @Override
    protected void doAppendTooltip(CompoundTag tag, BlockAccessor accessor, IPluginConfig config) {
        if (!config.get(ME_CRAFT_CPU) || !tag.contains(STATE_KEY, Tag.TAG_STRING)) {
            return;
        }
        var state = parseEnum(JobState.class, tag.getString(STATE_KEY));
        if (state.isEmpty()) {
            return;
        }
        add(helper.text(guiTr("cpu.state." + state.get().id).withStyle(ChatFormatting.GRAY)));
        if (tag.contains(TARGET_KEY, Tag.TAG_COMPOUND)) {
            appendTargetLine(tag);
        }
        if (state.get().busy() && tag.contains(TOTAL_STEPS_KEY, Tag.TAG_INT)) {
            var text = guiTr("cpu.steps",
                NUMBER_FORMAT.format(tag.getInt(COMPLETED_STEPS_KEY)),
                NUMBER_FORMAT.format(tag.getInt(TOTAL_STEPS_KEY))).withStyle(ChatFormatting.GRAY);
            add(helper.text(text));
        }
        if (tag.contains(MEMORY_LIMIT_KEY, Tag.TAG_LONG)) {
            var text = guiTr("memory",
                ClientUtil.getBytesString(tag.getLong(MEMORY_USAGE_KEY)),
                ClientUtil.getBytesString(tag.getLong(MEMORY_LIMIT_KEY))).withStyle(ChatFormatting.GRAY);
            add(helper.text(text));
        }
        var error = parseEnum(ExecutionError.class, tag.getString(ERROR_KEY)).orElse(ExecutionError.NONE);
        if (error != ExecutionError.NONE) {
            add(helper.text(guiTr("cpu.error." + error.id).withStyle(ChatFormatting.GRAY)));
        }
    }

    private void appendTargetLine(CompoundTag tag) {
        var key = decodeKey(tag.getCompound(TARGET_KEY));
        var amount = tag.getLong(TARGET_AMOUNT_KEY);
        var line = new ArrayList<IElement>();
        appendTargetIcon(line, key);
        line.add(helper.text(guiTr("cpu.target", "", ClientUtil.getNumberString(amount))));
        add(line);
    }

    private void appendTargetIcon(List<IElement> line, IStackKey key) {
        var display = key.display();
        if (display instanceof ItemRenderDescriptor item) {
            Waila.addItemIcon(line, helper, item.stack());
        } else if (display instanceof FluidRenderDescriptor fluid) {
            Waila.addFluidIcon(line, helper, fluid.stack());
        }
    }

    private static TranslatableComponent guiTr(String id, Object... args) {
        return I18n.tr("tinactory.gui.autocraft." + id, args);
    }

    private static <T extends Enum<T>> Optional<T> parseEnum(Class<T> clazz, String value) {
        try {
            return Optional.of(Enum.valueOf(clazz, value));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, ServerPlayer player, Level world,
        BlockEntity blockEntity, boolean showDetails) {
        var cpu = tryGetProvider(blockEntity, MECraftCpu.ID, MECraftCpu.class);
        if (cpu.isEmpty()) {
            return;
        }
        var status = cpu.get().status();
        tag.putString(STATE_KEY, status.state().name());
        if (!status.targets().isEmpty()) {
            var target = status.targets().get(0);
            tag.put(TARGET_KEY, encodeKey(target.key()));
            tag.putLong(TARGET_AMOUNT_KEY, target.amount());
        }
        tag.putInt(COMPLETED_STEPS_KEY, status.completedSteps());
        tag.putInt(TOTAL_STEPS_KEY, status.totalSteps());
        tag.putString(ERROR_KEY, status.error().name());
        tag.putLong(MEMORY_LIMIT_KEY, status.memoryLimit());
        tag.putLong(MEMORY_USAGE_KEY, status.memoryUsage());
    }

    private static CompoundTag encodeKey(IStackKey key) {
        var tag = new CompoundTag();
        tag.put("value", CodecHelper.encodeTag(StackHelper.KEY_CODEC, key));
        return tag;
    }

    private static IStackKey decodeKey(CompoundTag tag) {
        return CodecHelper.parseTag(StackHelper.KEY_CODEC, tag.get("value"));
    }
}
