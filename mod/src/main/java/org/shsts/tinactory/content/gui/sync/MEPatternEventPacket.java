package org.shsts.tinactory.content.gui.sync;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.MachineConstraintHelper;
import org.shsts.tinactory.core.autocraft.pattern.PatternNbtCodec;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternEventPacket implements IPacket {
    private static final PatternNbtCodec CODEC = new PatternNbtCodec(
        MachineConstraintHelper.CODEC,
        StackHelper.KEY_CODEC);

    private Action action = Action.CREATE;
    @Nullable
    private UUID patternUuid;
    @Nullable
    private CraftPattern pattern;

    public MEPatternEventPacket() {}

    private MEPatternEventPacket(Action action, @Nullable UUID patternUuid, @Nullable CraftPattern pattern) {
        this.action = action;
        this.patternUuid = patternUuid;
        this.pattern = pattern;
    }

    public static MEPatternEventPacket create(CraftPattern pattern) {
        return new MEPatternEventPacket(Action.CREATE, null, pattern);
    }

    public static MEPatternEventPacket update(UUID patternUuid, CraftPattern pattern) {
        return new MEPatternEventPacket(Action.UPDATE, patternUuid, pattern);
    }

    public static MEPatternEventPacket delete(UUID patternUuid) {
        return new MEPatternEventPacket(Action.DELETE, patternUuid, null);
    }

    public Action action() {
        return action;
    }

    @Nullable
    public UUID patternUuid() {
        return patternUuid;
    }

    @Nullable
    public CraftPattern pattern() {
        return pattern;
    }

    @Override
    public void serializeToBuf(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(action);
        buf.writeBoolean(patternUuid != null);
        if (patternUuid != null) {
            buf.writeUUID(patternUuid);
        }
        buf.writeNbt(pattern == null ? null : CODEC.encodePattern(pattern));
    }

    @Override
    public void deserializeFromBuf(RegistryFriendlyByteBuf buf) {
        action = buf.readEnum(Action.class);
        patternUuid = buf.readBoolean() ? buf.readUUID() : null;
        var patternTag = buf.readNbt();
        pattern = patternTag == null ? null : CODEC.decodePattern(patternTag);
    }

    public enum Action {
        CREATE,
        UPDATE,
        DELETE
    }
}
