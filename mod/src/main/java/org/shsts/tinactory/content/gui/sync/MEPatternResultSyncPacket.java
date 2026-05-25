package org.shsts.tinactory.content.gui.sync;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinycorelib.api.network.IPacket;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternResultSyncPacket implements IPacket {
    private ResultCode result = ResultCode.SUCCESS;
    @Nullable
    private String patternId;

    public MEPatternResultSyncPacket() {}

    public MEPatternResultSyncPacket(ResultCode result, @Nullable String patternId) {
        this.result = result;
        this.patternId = patternId;
    }

    public ResultCode result() {
        return result;
    }

    @Nullable
    public String patternId() {
        return patternId;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeEnum(result);
        buf.writeBoolean(patternId != null);
        if (patternId != null) {
            buf.writeUtf(patternId);
        }
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        result = buf.readEnum(ResultCode.class);
        patternId = buf.readBoolean() ? buf.readUtf() : null;
    }

    public enum ResultCode {
        SUCCESS,
        DUPLICATE_PATTERN_ID,
        PATTERN_NOT_FOUND,
        NO_CAPACITY,
        INVALID_PATTERN,
        STALE_PATTERN
    }
}
