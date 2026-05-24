package org.shsts.tinactory.content.gui.sync;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MECraftEventPacket implements IPacket {
    public enum Action {
        PREVIEW,
        EXECUTE,
        CANCEL
    }

    private Action action;
    @Nullable
    private IStackKey target;
    private long quantity;
    @Nullable
    private UUID cpuId;

    public MECraftEventPacket() {}

    private MECraftEventPacket(
        Action action,
        @Nullable IStackKey target,
        long quantity,
        @Nullable UUID cpuId) {

        this.action = action;
        this.target = target;
        this.quantity = quantity;
        this.cpuId = cpuId;
    }

    public static MECraftEventPacket preview(IStackKey target, long quantity) {
        return new MECraftEventPacket(Action.PREVIEW, target, quantity, null);
    }

    public static MECraftEventPacket execute(UUID cpuId) {
        return new MECraftEventPacket(Action.EXECUTE, null, 0L, cpuId);
    }

    public static MECraftEventPacket cancel(UUID cpuId) {
        return new MECraftEventPacket(Action.CANCEL, null, 0L, cpuId);
    }

    public Action action() {
        return action;
    }

    @Nullable
    public IStackKey target() {
        return target;
    }

    public long quantity() {
        return quantity;
    }

    @Nullable
    public UUID cpuId() {
        return cpuId;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeEnum(action);
        buf.writeBoolean(target != null);
        if (target != null) {
            buf.writeNbt((CompoundTag) CodecHelper.encodeTag(StackHelper.KEY_CODEC, target));
        }
        buf.writeLong(quantity);
        buf.writeBoolean(cpuId != null);
        if (cpuId != null) {
            buf.writeUUID(cpuId);
        }
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        action = buf.readEnum(Action.class);
        target = buf.readBoolean() ?
            CodecHelper.parseTag(StackHelper.KEY_CODEC, buf.readNbt()) :
            null;
        quantity = buf.readLong();
        cpuId = buf.readBoolean() ? buf.readUUID() : null;
    }
}
