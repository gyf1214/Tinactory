package org.shsts.tinactory.content.gui.sync;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinactory.core.autocraft.integration.AutocraftExecuteErrorCode;
import org.shsts.tinactory.core.autocraft.integration.AutocraftPreviewErrorCode;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftPreviewSyncPacket implements IPacket {
    @Nullable
    private UUID planId;
    private final List<CraftAmount> summaryOutputs = new ArrayList<>();
    @Nullable
    private AutocraftPreviewErrorCode previewError;
    @Nullable
    private AutocraftExecuteErrorCode executeError;

    public AutocraftPreviewSyncPacket() {}

    public AutocraftPreviewSyncPacket(
        @Nullable UUID planId,
        List<CraftAmount> summaryOutputs,
        @Nullable AutocraftPreviewErrorCode previewError,
        @Nullable AutocraftExecuteErrorCode executeError) {

        this.planId = planId;
        this.summaryOutputs.addAll(summaryOutputs);
        this.previewError = previewError;
        this.executeError = executeError;
    }

    @Nullable
    public UUID planId() {
        return planId;
    }

    public List<CraftAmount> summaryOutputs() {
        return List.copyOf(summaryOutputs);
    }

    @Nullable
    public AutocraftPreviewErrorCode previewError() {
        return previewError;
    }

    @Nullable
    public AutocraftExecuteErrorCode executeError() {
        return executeError;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeBoolean(planId != null);
        if (planId != null) {
            buf.writeUUID(planId);
        }
        buf.writeCollection(summaryOutputs, (buf1, amount) -> {
            buf1.writeEnum(amount.key().type());
            buf1.writeUtf(amount.key().id());
            buf1.writeUtf(amount.key().nbt());
            buf1.writeLong(amount.amount());
        });
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
        planId = buf.readBoolean() ? buf.readUUID() : null;
        summaryOutputs.clear();
        summaryOutputs.addAll(buf.readList(buf1 -> {
            var type = buf1.readEnum(CraftKey.Type.class);
            var id = buf1.readUtf();
            var nbt = buf1.readUtf();
            var amount = buf1.readLong();
            var key = type == CraftKey.Type.FLUID ? CraftKey.fluid(id, nbt) : CraftKey.item(id, nbt);
            return new CraftAmount(key, amount);
        }));
        previewError = buf.readBoolean() ? buf.readEnum(AutocraftPreviewErrorCode.class) : null;
        executeError = buf.readBoolean() ? buf.readEnum(AutocraftExecuteErrorCode.class) : null;
    }
}
