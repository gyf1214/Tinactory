package org.shsts.tinactory.content.gui.sync;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinactory.core.autocraft.pattern.CraftKey;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftEventPacket implements IPacket {
    public enum Action {
        PREVIEW,
        EXECUTE,
        CANCEL,
        CANCEL_CPU
    }

    private Action action;
    @Nullable
    private CraftKey target;
    private long quantity;
    @Nullable
    private UUID cpuId;

    public AutocraftEventPacket() {}

    private AutocraftEventPacket(
        Action action,
        @Nullable CraftKey target,
        long quantity,
        @Nullable UUID cpuId) {

        this.action = action;
        this.target = target;
        this.quantity = quantity;
        this.cpuId = cpuId;
    }

    public static AutocraftEventPacket preview(CraftKey target, long quantity, UUID cpuId) {
        return new AutocraftEventPacket(Action.PREVIEW, target, quantity, cpuId);
    }

    public static AutocraftEventPacket execute(UUID cpuId) {
        return new AutocraftEventPacket(Action.EXECUTE, null, 0L, cpuId);
    }

    public static AutocraftEventPacket cancel() {
        return new AutocraftEventPacket(Action.CANCEL, null, 0L, null);
    }

    public static AutocraftEventPacket cancelCpu(UUID cpuId) {
        return new AutocraftEventPacket(Action.CANCEL_CPU, null, 0L, cpuId);
    }

    public Action action() {
        return action;
    }

    @Nullable
    public CraftKey target() {
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
            buf.writeEnum(target.type());
            buf.writeUtf(target.id());
            buf.writeUtf(target.nbt());
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
        target = buf.readBoolean() ? new CraftKey(
            buf.readEnum(CraftKey.Type.class),
            buf.readUtf(),
            buf.readUtf()) : null;
        quantity = buf.readLong();
        cpuId = buf.readBoolean() ? buf.readUUID() : null;
    }
}
