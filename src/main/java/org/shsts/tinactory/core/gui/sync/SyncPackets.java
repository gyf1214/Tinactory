package org.shsts.tinactory.core.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class SyncPackets {
    public static class Double implements IPacket {
        private double data;

        public Double() {}

        public Double(double data) {
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
            if (!(o instanceof Double that)) {
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
}
