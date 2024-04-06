package org.shsts.tinactory.core.gui.sync;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.common.IPacket;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MenuSyncPacket implements IPacket {
    protected int containerId;
    protected int index;

    protected MenuSyncPacket() {}

    protected MenuSyncPacket(int containerId, int index) {
        this.containerId = containerId;
        this.index = index;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeVarInt(index);
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        containerId = buf.readVarInt();
        index = buf.readVarInt();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuSyncPacket that = (MenuSyncPacket) o;
        return containerId == that.containerId && index == that.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(containerId, index);
    }

    public int getContainerId() {
        return containerId;
    }

    public int getIndex() {
        return index;
    }

    public static class Boolean extends MenuSyncPacket {
        private boolean value;

        public Boolean() {}

        public Boolean(int containerId, int index, boolean value) {
            super(containerId, index);
            this.value = value;
        }

        @Override
        public void serializeToBuf(FriendlyByteBuf buf) {
            super.serializeToBuf(buf);
            buf.writeBoolean(value);
        }

        @Override
        public void deserializeFromBuf(FriendlyByteBuf buf) {
            super.deserializeFromBuf(buf);
            value = buf.readBoolean();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Boolean that)) return false;
            if (!super.equals(o)) return false;
            return value == that.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), value);
        }

        public boolean getValue() {
            return value;
        }
    }

    public static class Double extends MenuSyncPacket {
        private double data;

        public Double() {}

        public Double(int containerId, int index, double data) {
            super(containerId, index);
            this.data = data;
        }

        @Override
        public void serializeToBuf(FriendlyByteBuf buf) {
            super.serializeToBuf(buf);
            buf.writeDouble(data);
        }

        @Override
        public void deserializeFromBuf(FriendlyByteBuf buf) {
            super.deserializeFromBuf(buf);
            data = buf.readDouble();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Double that)) return false;
            if (!super.equals(o)) return false;
            return java.lang.Double.compare(that.data, data) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), data);
        }

        public double getData() {
            return data;
        }
    }

    public static class Long extends MenuSyncPacket {
        private long data;

        public Long() {}

        public Long(int containerId, int index, long data) {
            super(containerId, index);
            this.data = data;
        }

        @Override
        public void serializeToBuf(FriendlyByteBuf buf) {
            super.serializeToBuf(buf);
            buf.writeVarLong(data);
        }

        @Override
        public void deserializeFromBuf(FriendlyByteBuf buf) {
            super.deserializeFromBuf(buf);
            data = buf.readVarLong();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Long that)) return false;
            if (!super.equals(o)) return false;
            return data == that.data;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), data);
        }

        public long getData() {
            return data;
        }
    }

    public static abstract class Holder<T> extends MenuSyncPacket {
        @Nullable
        private T data;

        public Holder() {}

        public Holder(int containerId, int index, @Nullable T data) {
            super(containerId, index);
            this.data = data;
        }

        protected abstract void dataToBuf(FriendlyByteBuf buf, T data);

        protected abstract T dataFromBuf(FriendlyByteBuf buf);

        @Override
        public void serializeToBuf(FriendlyByteBuf buf) {
            super.serializeToBuf(buf);
            buf.writeBoolean(data != null);
            if (data != null) {
                dataToBuf(buf, data);
            }
        }

        @Override
        public void deserializeFromBuf(FriendlyByteBuf buf) {
            super.deserializeFromBuf(buf);
            var present = buf.readBoolean();
            data = present ? dataFromBuf(buf) : null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Holder<?> other)) return false;
            if (!super.equals(o)) return false;
            return Objects.equals(data, other.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), data);
        }

        public Optional<T> getData() {
            return Optional.ofNullable(data);
        }
    }

    public static class LocHolder extends Holder<ResourceLocation> {
        public LocHolder() {}

        public LocHolder(int containerId, int index, @Nullable ResourceLocation data) {
            super(containerId, index, data);
        }

        @Override
        protected void dataToBuf(FriendlyByteBuf buf, ResourceLocation data) {
            buf.writeResourceLocation(data);
        }

        @Override
        protected ResourceLocation dataFromBuf(FriendlyByteBuf buf) {
            return buf.readResourceLocation();
        }
    }
}
