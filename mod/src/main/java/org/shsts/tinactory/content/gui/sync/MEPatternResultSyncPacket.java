package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinycorelib.api.network.IPacket;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternResultSyncPacket implements IPacket {
    private ResultCode result = ResultCode.SUCCESS;

    public MEPatternResultSyncPacket() {}

    public MEPatternResultSyncPacket(ResultCode result) {
        this.result = result;
    }

    public ResultCode result() {
        return result;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeEnum(result);
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        result = buf.readEnum(ResultCode.class);
    }

    public enum ResultCode {
        SUCCESS,
        DUPLICATE_PATTERN_ID,
        PATTERN_NOT_FOUND,
        NO_CAPACITY,
        INVALID_PATTERN
    }
}
