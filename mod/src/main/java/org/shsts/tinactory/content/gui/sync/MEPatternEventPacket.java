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

    private String patternId = "";
    @Nullable
    private CraftPattern pattern;

    public MEPatternEventPacket() {}

    public MEPatternEventPacket(String patternId, @Nullable CraftPattern pattern) {
        this.patternId = patternId;
        this.pattern = pattern;
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
        buf.writeUtf(patternId);
        buf.writeBoolean(pattern != null);
        if (pattern != null) {
            buf.writeNbt(CODEC.encodePattern(pattern));
        }
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        patternId = buf.readUtf();
        pattern = buf.readBoolean() ? CODEC.decodePattern(buf.readNbt()) : null;
    }
}
