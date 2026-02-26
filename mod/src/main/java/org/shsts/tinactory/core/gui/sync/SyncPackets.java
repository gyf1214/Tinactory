package org.shsts.tinactory.core.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class SyncPackets {
    public static class DoublePacket implements IPacket {
        private double data;

        public DoublePacket() {}

        public DoublePacket(double data) {
            this.data = data;
        }

        @Override
        public void serializeToBuf(FriendlyByteBuf buf) {
            buf.writeDouble(data);
        }

        @Override
        public void deserializeFromBuf(FriendlyByteBuf buf) {
            data = buf.readDouble();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DoublePacket that)) {
                return false;
            }
            return that.data == data;
        }

        @Override
        public int hashCode() {
            return Objects.hash(data);
        }

        public double getData() {
            return data;
        }
    }

    public static DoublePacket doublePacket(double data) {
        return new DoublePacket(data);
    }

    public static class LongPacket implements IPacket {
        private long data;

        public LongPacket() {}

        public LongPacket(long data) {
            this.data = data;
        }

        @Override
        public void serializeToBuf(FriendlyByteBuf buf) {
            buf.writeLong(data);
        }

        @Override
        public void deserializeFromBuf(FriendlyByteBuf buf) {
            data = buf.readLong();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof LongPacket that)) {
                return false;
            }
            return that.data == data;
        }

        @Override
        public int hashCode() {
            return Objects.hash(data);
        }

        public long getData() {
            return data;
        }
    }

    public static LongPacket longPacket(long data) {
        return new LongPacket(data);
    }
}
