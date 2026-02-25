package org.shsts.tinactory.content.gui.sync;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinactory.core.autocraft.integration.AutocraftRequestableKey;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftTerminalActionPacket implements IPacket {
    public enum Action {
        PREVIEW,
        EXECUTE,
        CANCEL,
        CANCEL_CPU
    }

    private Action action;
    @Nullable
    private AutocraftRequestableKey target;
    private long quantity;
    @Nullable
    private UUID cpuId;
    @Nullable
    private UUID planId;

    public AutocraftTerminalActionPacket() {}

    private AutocraftTerminalActionPacket(
        Action action,
        @Nullable AutocraftRequestableKey target,
        long quantity,
        @Nullable UUID cpuId,
        @Nullable UUID planId) {

        this.action = action;
        this.target = target;
        this.quantity = quantity;
        this.cpuId = cpuId;
        this.planId = planId;
    }

    public static AutocraftTerminalActionPacket preview(AutocraftRequestableKey target, long quantity, UUID cpuId) {
        return new AutocraftTerminalActionPacket(Action.PREVIEW, target, quantity, cpuId, null);
    }

    public static AutocraftTerminalActionPacket execute(UUID planId, UUID cpuId) {
        return new AutocraftTerminalActionPacket(Action.EXECUTE, null, 0L, cpuId, planId);
    }

    public static AutocraftTerminalActionPacket cancel(UUID planId) {
        return new AutocraftTerminalActionPacket(Action.CANCEL, null, 0L, null, planId);
    }

    public static AutocraftTerminalActionPacket cancelCpu(UUID cpuId) {
        return new AutocraftTerminalActionPacket(Action.CANCEL_CPU, null, 0L, cpuId, null);
    }

    public Action action() {
        return action;
    }

    @Nullable
    public AutocraftRequestableKey target() {
        return target;
    }

    public long quantity() {
        return quantity;
    }

    @Nullable
    public UUID cpuId() {
        return cpuId;
    }

    @Nullable
    public UUID planId() {
        return planId;
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
        buf.writeBoolean(planId != null);
        if (planId != null) {
            buf.writeUUID(planId);
        }
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        action = buf.readEnum(Action.class);
        target = buf.readBoolean() ? new AutocraftRequestableKey(
            buf.readEnum(CraftKey.Type.class),
            buf.readUtf(),
            buf.readUtf()) : null;
        quantity = buf.readLong();
        cpuId = buf.readBoolean() ? buf.readUUID() : null;
        planId = buf.readBoolean() ? buf.readUUID() : null;
    }
}
