package org.shsts.tinactory.content.gui.sync;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.core.autocraft.service.AutocraftExecuteResult;
import org.shsts.tinactory.core.autocraft.service.AutocraftPreviewResult;
import org.shsts.tinactory.integration.logistics.IngredientKeyCodecHelper;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftPreviewSyncPacket implements IPacket {
    @Nullable
    private List<CraftAmount> targets;
    @Nullable
    private AutocraftPreviewResult.Code previewError;
    @Nullable
    private AutocraftExecuteResult.Code executeError;

    public AutocraftPreviewSyncPacket() {}

    private AutocraftPreviewSyncPacket(
        @Nullable List<CraftAmount> targets,
        @Nullable AutocraftPreviewResult.Code previewError,
        @Nullable AutocraftExecuteResult.Code executeError) {

        this.targets = targets != null ? List.copyOf(targets) : null;
        this.previewError = previewError;
        this.executeError = executeError;
    }

    public static AutocraftPreviewSyncPacket preview(AutocraftPreviewResult result) {
        return new AutocraftPreviewSyncPacket(result.targets(), result.errorCode(), null);
    }

    public static AutocraftPreviewSyncPacket execute(AutocraftExecuteResult result) {
        return new AutocraftPreviewSyncPacket(null, null, result.errorCode());
    }

    public static AutocraftPreviewSyncPacket cancel() {
        return new AutocraftPreviewSyncPacket(null, null, null);
    }

    @Nullable
    public List<CraftAmount> targets() {
        return targets;
    }

    @Nullable
    public AutocraftPreviewResult.Code previewError() {
        return previewError;
    }

    @Nullable
    public AutocraftExecuteResult.Code executeError() {
        return executeError;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeBoolean(targets != null);
        if (targets != null) {
            buf.writeCollection(targets, (buf1, amount) -> {
                buf1.writeNbt((CompoundTag) CodecHelper.encodeTag(IngredientKeyCodecHelper.CODEC, amount.key()));
                buf1.writeLong(amount.amount());
            });
        }
        buf.writeBoolean(previewError != null);
        if (previewError != null) {
            buf.writeEnum(previewError);
        }
        buf.writeBoolean(executeError != null);
        if (executeError != null) {
            buf.writeEnum(executeError);
        }
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        targets = buf.readBoolean() ? buf.readList(buf1 -> {
            var key = CodecHelper.parseTag(IngredientKeyCodecHelper.CODEC, buf1.readNbt());
            var amount = buf1.readLong();
            return new CraftAmount(key, amount);
        }) : null;
        previewError = buf.readBoolean() ? buf.readEnum(AutocraftPreviewResult.Code.class) : null;
        executeError = buf.readBoolean() ? buf.readEnum(AutocraftExecuteResult.Code.class) : null;
    }
}
