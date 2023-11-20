package org.shsts.tinactory.gui.sync;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinactory.core.IPacket;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ContainerSyncPacket implements IPacket {
    public final int containerId;
    public final int index;

    protected ContainerSyncPacket(int containerId, int index) {
        this.containerId = containerId;
        this.index = index;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeVarInt(this.containerId);
        buf.writeVarInt(this.index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        ContainerSyncPacket that = (ContainerSyncPacket) o;
        return this.containerId == that.containerId && this.index == that.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.containerId, this.index);
    }

    public static class Double extends ContainerSyncPacket {
        public final double data;

        public Double(int containerId, int index, double data) {
            super(containerId, index);
            this.data = data;
        }

        @Override
        public void serializeToBuf(FriendlyByteBuf buf) {
            super.serializeToBuf(buf);
            buf.writeDouble(this.data);
        }

        public static Double create(FriendlyByteBuf buf) {
            var containerId = buf.readVarInt();
            var index = buf.readVarInt();
            var data = buf.readDouble();
            return new Double(containerId, index, data);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Double that)) return false;
            if (!super.equals(o)) return false;
            return java.lang.Double.compare(that.data, this.data) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), data);
        }
    }

    public static class Long extends ContainerSyncPacket {
        public final long data;

        public Long(int containerId, int index, long data) {
            super(containerId, index);
            this.data = data;
        }

        @Override
        public void serializeToBuf(FriendlyByteBuf buf) {
            super.serializeToBuf(buf);
            buf.writeVarLong(this.data);
        }

        public static Long create(FriendlyByteBuf buf) {
            var containerId = buf.readVarInt();
            var index = buf.readVarInt();
            var data = buf.readVarLong();
            return new Long(containerId, index, data);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Long that)) return false;
            if (!super.equals(o)) return false;
            return this.data == that.data;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), this.data);
        }
    }
}
