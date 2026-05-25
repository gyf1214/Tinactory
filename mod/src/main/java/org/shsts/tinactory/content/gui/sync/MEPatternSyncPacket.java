package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.MachineConstraintHelper;
import org.shsts.tinactory.core.autocraft.pattern.PatternNbtCodec;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternSyncPacket implements IPacket {
    private static final PatternNbtCodec CODEC = new PatternNbtCodec(
        MachineConstraintHelper.CODEC,
        StackHelper.KEY_CODEC);

    private final List<CraftPattern> patterns = new ArrayList<>();

    public MEPatternSyncPacket() {}

    public MEPatternSyncPacket(List<CraftPattern> patterns) {
        this.patterns.addAll(patterns);
    }

    public List<CraftPattern> patterns() {
        return patterns;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeCollection(patterns, (buf1, pattern) -> buf1.writeNbt(CODEC.encodePattern(pattern)));
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        patterns.clear();
        patterns.addAll(buf.readList(buf1 -> CODEC.decodePattern(buf1.readNbt())));
    }
}
