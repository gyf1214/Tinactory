package org.shsts.tinactory.core.gui.sync;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinactory.core.common.IPacket;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ContainerSyncPacket implements IPacket {
    protected int containerId;
    protected int index;

    protected ContainerSyncPacket() {}

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
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        this.containerId = buf.readVarInt();
        this.index = buf.readVarInt();
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

    public int getContainerId() {
        return this.containerId;
    }

    public int getIndex() {
        return this.index;
    }

    public static class Boolean extends ContainerSyncPacket {
        private boolean value;

        public Boolean() {}

        public Boolean(int containerId, int index, boolean value) {
            super(containerId, index);
            this.value = value;
        }

        @Override
        public void serializeToBuf(FriendlyByteBuf buf) {
            super.serializeToBuf(buf);
            buf.writeBoolean(this.value);
        }

        @Override
        public void deserializeFromBuf(FriendlyByteBuf buf) {
            super.deserializeFromBuf(buf);
            this.value = buf.readBoolean();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Boolean that)) return false;
            if (!super.equals(o)) return false;
            return this.value == that.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), this.value);
        }

        public boolean getValue() {
            return this.value;
        }
    }

    public static class Double extends ContainerSyncPacket {
        private double data;

        public Double() {}

        public Double(int containerId, int index, double data) {
            super(containerId, index);
            this.data = data;
        }

        @Override
        public void serializeToBuf(FriendlyByteBuf buf) {
            super.serializeToBuf(buf);
            buf.writeDouble(this.data);
        }

        @Override
        public void deserializeFromBuf(FriendlyByteBuf buf) {
            super.deserializeFromBuf(buf);
            this.data = buf.readDouble();
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
            return Objects.hash(super.hashCode(), this.data);
        }

        public double getData() {
            return this.data;
        }
    }

    public static class Long extends ContainerSyncPacket {
        private long data;

        public Long() {}

        public Long(int containerId, int index, long data) {
            super(containerId, index);
            this.data = data;
        }

        @Override
        public void serializeToBuf(FriendlyByteBuf buf) {
            super.serializeToBuf(buf);
            buf.writeVarLong(this.data);
        }

        @Override
        public void deserializeFromBuf(FriendlyByteBuf buf) {
            super.deserializeFromBuf(buf);
            this.data = buf.readVarLong();
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

        public long getData() {
            return this.data;
        }
    }
}
