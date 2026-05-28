package org.shsts.tinactory.content.gui.sync;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.core.autocraft.plan.PlanError;
import org.shsts.tinactory.core.autocraft.plan.PlanSummary;
import org.shsts.tinactory.core.autocraft.service.AutocraftPreview;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.LinkedHashMap;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MECraftPreviewSyncPacket implements IPacket {
    private PreviewState state = PreviewState.EMPTY;
    @Nullable
    private PlanError error;
    private PlanSummary summary = PlanSummary.empty();

    public MECraftPreviewSyncPacket() {}

    private MECraftPreviewSyncPacket(
        PreviewState state,
        @Nullable PlanError error,
        PlanSummary summary) {
        this.state = state;
        this.error = error;
        this.summary = summary;
    }

    public static MECraftPreviewSyncPacket of(AutocraftPreview preview) {
        if (preview.isSuccess()) {
            return ready(preview.summary());
        }
        if (preview.error() != null) {
            return failed(preview.error(), preview.summary());
        }
        return empty();
    }

    public static MECraftPreviewSyncPacket empty() {
        return new MECraftPreviewSyncPacket(PreviewState.EMPTY, null, PlanSummary.empty());
    }

    public static MECraftPreviewSyncPacket ready(PlanSummary summary) {
        return new MECraftPreviewSyncPacket(PreviewState.PREVIEW_READY, null, summary);
    }

    public static MECraftPreviewSyncPacket failed(PlanError error, PlanSummary summary) {
        return new MECraftPreviewSyncPacket(PreviewState.PREVIEW_FAILED, error, summary);
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

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeEnum(state);
        buf.writeNbt(error == null ? null : serializeError(error));
        buf.writeCollection(summary.entries().entrySet(), (buf1, entry) -> {
            buf1.writeNbt(encodeIngredientKey(entry.getKey()));
            buf1.writeLong(entry.getValue().existingAmount());
            buf1.writeLong(entry.getValue().consumedFromInventory());
            buf1.writeLong(entry.getValue().craftedAmount());
        });
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        state = buf.readEnum(PreviewState.class);
        error = deserializeError(buf.readNbt());
        var entries = new LinkedHashMap<IStackKey, PlanSummary.Entry>();
        for (var entry : buf.readList(buf1 -> {
            var key = decodeIngredientKey(CodecHelper.readRequiredNbt(buf1, "summary key"));
            var summaryEntry = new PlanSummary.Entry(buf1.readLong(), buf1.readLong(), buf1.readLong());
            return new SummaryEntry(key, summaryEntry);
        })) {
            entries.put(entry.key(), entry.entry());
        }
        summary = new PlanSummary(entries);
    }

    private static CompoundTag serializeError(PlanError error) {
        var tag = new CompoundTag();
        tag.putString("code", error.code().name());
        tag.put("targetKey", encodeIngredientKey(error.targetKey()));
        return tag;
    }

    @Nullable
    private static PlanError deserializeError(@Nullable CompoundTag tag) {
        if (tag == null) {
            return null;
        }
        return new PlanError(
            PlanError.Code.valueOf(tag.getString("code")),
            decodeIngredientKey(tag.getCompound("targetKey")));
    }

    private static CompoundTag encodeIngredientKey(IStackKey key) {
        var tag = new CompoundTag();
        tag.put("value", CodecHelper.encodeTag(StackHelper.KEY_CODEC, key));
        return tag;
    }

    private static IStackKey decodeIngredientKey(CompoundTag tag) {
        return CodecHelper.parseTag(StackHelper.KEY_CODEC, tag.get("value"));
    }

    private record SummaryEntry(IStackKey key, PlanSummary.Entry entry) {}

    public enum PreviewState {
        EMPTY,
        PREVIEW_READY,
        PREVIEW_FAILED
    }
}
