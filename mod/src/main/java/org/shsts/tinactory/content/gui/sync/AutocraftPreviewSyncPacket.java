package org.shsts.tinactory.content.gui.sync;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.plan.PlanError;
import org.shsts.tinactory.core.autocraft.service.AutocraftPreviewResult;
import org.shsts.tinactory.core.logistics.IStackKey;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.nbt.Tag.TAG_COMPOUND;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftPreviewSyncPacket implements IPacket {
    private final Codec<IStackKey> ingredientKeyCodec;
    private PreviewState state = PreviewState.EMPTY;
    @Nullable
    private List<CraftAmount> targets;
    @Nullable
    private PlanError error;

    public AutocraftPreviewSyncPacket() {
        this(StackHelper.KEY_CODEC);
    }

    public AutocraftPreviewSyncPacket(Codec<IStackKey> ingredientKeyCodec) {
        this.ingredientKeyCodec = ingredientKeyCodec;
    }

    private AutocraftPreviewSyncPacket(
        Codec<IStackKey> ingredientKeyCodec,
        PreviewState state,
        @Nullable List<CraftAmount> targets,
        @Nullable PlanError error) {
        this.ingredientKeyCodec = ingredientKeyCodec;
        this.state = state;
        this.targets = targets != null ? List.copyOf(targets) : null;
        this.error = error;
    }

    public static AutocraftPreviewSyncPacket of(AutocraftPreviewResult result) {
        if (result.isSuccess()) {
            return ready(result.targets());
        }
        if (!result.isEmpty()) {
            return failed(result.error());
        }
        return empty();
    }

    public static AutocraftPreviewSyncPacket empty() {
        return new AutocraftPreviewSyncPacket(StackHelper.KEY_CODEC, PreviewState.EMPTY, null, null);
    }

    public static AutocraftPreviewSyncPacket ready(List<CraftAmount> targets) {
        return new AutocraftPreviewSyncPacket(
            StackHelper.KEY_CODEC,
            PreviewState.PREVIEW_READY,
            targets,
            null);
    }

    public static AutocraftPreviewSyncPacket failed(PlanError error) {
        return new AutocraftPreviewSyncPacket(
            StackHelper.KEY_CODEC,
            PreviewState.PREVIEW_FAILED,
            null,
            error);
    }

    public static AutocraftPreviewSyncPacket empty(Codec<IStackKey> ingredientKeyCodec) {
        return new AutocraftPreviewSyncPacket(ingredientKeyCodec, PreviewState.EMPTY, null, null);
    }

    public static AutocraftPreviewSyncPacket ready(
        Codec<IStackKey> ingredientKeyCodec,
        List<CraftAmount> targets) {
        return new AutocraftPreviewSyncPacket(ingredientKeyCodec, PreviewState.PREVIEW_READY, targets, null);
    }

    public static AutocraftPreviewSyncPacket failed(Codec<IStackKey> ingredientKeyCodec, PlanError error) {
        return new AutocraftPreviewSyncPacket(ingredientKeyCodec, PreviewState.PREVIEW_FAILED, null, error);
    }

    public PreviewState state() {
        return state;
    }

    @Nullable
    public List<CraftAmount> targets() {
        return targets;
    }

    @Nullable
    public PlanError error() {
        return error;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeEnum(state);
        buf.writeBoolean(targets != null);
        if (targets != null) {
            buf.writeCollection(targets, (buf1, amount) -> {
                buf1.writeNbt(encodeIngredientKey(ingredientKeyCodec, amount.key()));
                buf1.writeLong(amount.amount());
            });
        }
        buf.writeBoolean(error != null);
        if (error != null) {
            buf.writeNbt(serializeError(error, ingredientKeyCodec));
        }
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        state = buf.readEnum(PreviewState.class);
        targets = buf.readBoolean() ? buf.readList(buf1 -> {
            var key = decodeIngredientKey(ingredientKeyCodec, buf1.readNbt());
            var amount = buf1.readLong();
            return new CraftAmount(key, amount);
        }) : null;
        error = buf.readBoolean() ? deserializeError(buf.readNbt(), ingredientKeyCodec) : null;
    }

    private static CompoundTag serializeError(PlanError error, Codec<IStackKey> ingredientKeyCodec) {
        var tag = new CompoundTag();
        tag.putString("code", error.code().name());
        if (error.targetKey() != null) {
            tag.put("targetKey", encodeIngredientKey(ingredientKeyCodec, error.targetKey()));
        }
        var cyclePath = new ListTag();
        for (var key : error.cyclePath()) {
            cyclePath.add(encodeIngredientKey(ingredientKeyCodec, key));
        }
        tag.put("cyclePath", cyclePath);
        return tag;
    }

    private static PlanError deserializeError(@Nullable CompoundTag tag, Codec<IStackKey> ingredientKeyCodec) {
        if (tag == null) {
            return PlanError.none();
        }
        var cyclePathTag = tag.getList("cyclePath", TAG_COMPOUND);
        var cyclePath = new ArrayList<IStackKey>(cyclePathTag.size());
        for (var i = 0; i < cyclePathTag.size(); i++) {
            cyclePath.add(decodeIngredientKey(ingredientKeyCodec, cyclePathTag.getCompound(i)));
        }
        var targetKey = tag.contains("targetKey", TAG_COMPOUND) ?
            decodeIngredientKey(ingredientKeyCodec, tag.getCompound("targetKey")) :
            null;
        return new PlanError(PlanError.Code.valueOf(tag.getString("code")), targetKey, cyclePath);
    }

    private static CompoundTag encodeIngredientKey(Codec<IStackKey> ingredientKeyCodec, IStackKey key) {
        var tag = new CompoundTag();
        tag.put("value", CodecHelper.encodeTag(ingredientKeyCodec, key));
        return tag;
    }

    private static IStackKey decodeIngredientKey(
        Codec<IStackKey> ingredientKeyCodec,
        @Nullable CompoundTag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Missing ingredient key payload");
        }
        return CodecHelper.parseTag(ingredientKeyCodec, tag.get("value"));
    }

    public enum PreviewState {
        EMPTY,
        PREVIEW_READY,
        PREVIEW_FAILED
    }
}
