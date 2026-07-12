package org.shsts.tinactory.content.gui.sync;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.core.autocraft.plan.PlanError;
import org.shsts.tinactory.core.autocraft.plan.PlanResult;
import org.shsts.tinactory.core.autocraft.plan.PlanSummary;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.LinkedHashMap;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MECraftPreviewSyncPacket implements IPacket {
    private PreviewState state = PreviewState.EMPTY;
    @Nullable
    private PlanError error;
    private PlanSummary summary = PlanSummary.empty();
    private long memoryUsage;

    public MECraftPreviewSyncPacket() {}

    private MECraftPreviewSyncPacket(
        PreviewState state,
        @Nullable PlanError error,
        PlanSummary summary,
        long memoryUsage) {
        this.state = state;
        this.error = error;
        this.summary = summary;
        this.memoryUsage = memoryUsage;
    }

    public static MECraftPreviewSyncPacket of(Optional<PlanResult> preview) {
        if (preview.isEmpty()) {
            return empty();
        }
        var result = preview.get();
        if (result.plan() != null) {
            return ready(result.plan().summary(), result.plan().memoryUsage());
        }
        return failed(result.error(), result.summary());
    }

    public static MECraftPreviewSyncPacket empty() {
        return new MECraftPreviewSyncPacket(PreviewState.EMPTY, null, PlanSummary.empty(), 0L);
    }

    public static MECraftPreviewSyncPacket ready(PlanSummary summary) {
        return ready(summary, 0L);
    }

    public static MECraftPreviewSyncPacket ready(PlanSummary summary, long memoryUsage) {
        return new MECraftPreviewSyncPacket(PreviewState.PREVIEW_READY, null, summary, memoryUsage);
    }

    public static MECraftPreviewSyncPacket failed(PlanError error, PlanSummary summary) {
        return new MECraftPreviewSyncPacket(PreviewState.PREVIEW_FAILED, error, summary, 0L);
    }

    public PreviewState state() {
        return state;
    }

    @Nullable
    public PlanError error() {
        return error;
    }

    public PlanSummary summary() {
        return summary;
    }

    public long memoryUsage() {
        return memoryUsage;
    }

    @Override
    public void serializeToBuf(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(state);
        buf.writeLong(memoryUsage);

        CodecHelper.encodeOptionalToBuf(buf, Optional.ofNullable(error), MECraftPreviewSyncPacket::serializeError);
        CodecHelper.encodeCollectionToBuf(buf, summary.entries().entrySet(), (buf1, entry) -> {
            StackHelper.KEY_STREAM_CODEC.encode(buf1, entry.getKey());
            buf1.writeLong(entry.getValue().existingAmount());
            buf1.writeLong(entry.getValue().consumedFromInventory());
            buf1.writeLong(entry.getValue().craftedAmount());
        });
    }

    @Override
    public void deserializeFromBuf(RegistryFriendlyByteBuf buf) {
        state = buf.readEnum(PreviewState.class);
        memoryUsage = buf.readLong();
        error = CodecHelper.parseOptionalFromBuf(buf, MECraftPreviewSyncPacket::deserializeError).orElse(null);

        var entries = new LinkedHashMap<IStackKey, PlanSummary.Entry>();
        CodecHelper.parseWithCountFromBuf(buf, buf1 -> {
            var key = StackHelper.KEY_STREAM_CODEC.decode(buf1);
            var entry = new PlanSummary.Entry(buf1.readLong(), buf1.readLong(), buf1.readLong());
            entries.put(key, entry);
        });
        summary = new PlanSummary(entries);
    }

    private static void serializeError(RegistryFriendlyByteBuf buf, PlanError error) {
        buf.writeEnum(error.code());
        StackHelper.KEY_STREAM_CODEC.encode(buf, error.targetKey());
    }

    private static PlanError deserializeError(RegistryFriendlyByteBuf buf) {
        return new PlanError(buf.readEnum(PlanError.Code.class),
            StackHelper.KEY_STREAM_CODEC.decode(buf));
    }

    public enum PreviewState {
        EMPTY,
        PREVIEW_READY,
        PREVIEW_FAILED
    }
}
