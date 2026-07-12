package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.integration.autocraft.PatternHelper.PATTERN_CODECS;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternSyncPacket implements IPacket {
    private final List<CraftPattern> patterns = new ArrayList<>();

    public MEPatternSyncPacket() {}

    public MEPatternSyncPacket(List<CraftPattern> patterns) {
        this.patterns.addAll(patterns);
    }

    public List<CraftPattern> patterns() {
        return patterns;
    }

    @Override
    public void serializeToBuf(RegistryFriendlyByteBuf buf) {
        CodecHelper.encodeCollectionToBuf(buf, patterns, PATTERN_CODECS::encodePatternToBuf);
    }

    @Override
    public void deserializeFromBuf(RegistryFriendlyByteBuf buf) {
        patterns.clear();
        patterns.addAll(CodecHelper.parseListFromBuf(buf, PATTERN_CODECS::decodePatternFromBuf));
    }
}
