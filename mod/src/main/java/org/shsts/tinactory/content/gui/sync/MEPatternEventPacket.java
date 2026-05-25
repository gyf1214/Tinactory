package org.shsts.tinactory.content.gui.sync;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.MachineConstraintHelper;
import org.shsts.tinactory.core.autocraft.pattern.PatternNbtCodec;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.network.IPacket;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternEventPacket implements IPacket {
    private static final PatternNbtCodec CODEC = new PatternNbtCodec(
        MachineConstraintHelper.CODEC,
        StackHelper.KEY_CODEC);

    private Action action = Action.CREATE;
    private String patternId = "";
    @Nullable
    private CraftPattern pattern;

    public MEPatternEventPacket() {}

    private MEPatternEventPacket(Action action, String patternId, @Nullable CraftPattern pattern) {
        this.action = action;
        this.patternId = patternId;
        this.pattern = pattern;
    }

    public static MEPatternEventPacket createPattern(CraftPattern pattern) {
        return new MEPatternEventPacket(Action.CREATE, pattern.patternId(), pattern);
    }

    public static MEPatternEventPacket updatePattern(String patternId, CraftPattern pattern) {
        return new MEPatternEventPacket(Action.UPDATE, patternId, pattern);
    }

    public static MEPatternEventPacket deletePattern(String patternId) {
        return new MEPatternEventPacket(Action.DELETE, patternId, null);
    }

    public Action action() {
        return action;
    }

    public String patternId() {
        return patternId;
    }

    @Nullable
    public CraftPattern pattern() {
        return pattern;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeEnum(action);
        buf.writeUtf(patternId);
        buf.writeBoolean(pattern != null);
        if (pattern != null) {
            buf.writeNbt(CODEC.encodePattern(pattern));
        }
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        action = buf.readEnum(Action.class);
        patternId = buf.readUtf();
        pattern = buf.readBoolean() ? CODEC.decodePattern(buf.readNbt()) : null;
    }

    public enum Action {
        CREATE,
        UPDATE,
        DELETE
    }
}
