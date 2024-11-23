package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinactory.content.logistics.LogisticWorker;
import org.shsts.tinactory.core.gui.sync.MenuSyncPacket;
import org.shsts.tinactory.core.util.CodecHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LogisticWorkerSyncPacket extends MenuSyncPacket {
    public record PortInfo(UUID machineId, int portIndex, Component machineName, ItemStack icon, Component portName) {
        public static void serialize(FriendlyByteBuf buf, PortInfo info) {
            buf.writeUUID(info.machineId);
            buf.writeVarInt(info.portIndex);
            buf.writeUtf(CodecHelper.encodeComponent(info.machineName));
            var jo = CodecHelper.encodeJson(ItemStack.CODEC, info.icon);
            buf.writeUtf(CodecHelper.jsonToStr(jo));
            buf.writeUtf(CodecHelper.encodeComponent(info.portName));
        }

        public static PortInfo deserialize(FriendlyByteBuf buf) {
            return new PortInfo(buf.readUUID(), buf.readVarInt(),
                CodecHelper.parseComponent(buf.readUtf()),
                CodecHelper.parseJson(ItemStack.CODEC, CodecHelper.jsonFromStr(buf.readUtf())),
                CodecHelper.parseComponent(buf.readUtf()));
        }

        public LogisticComponent.PortKey getKey() {
            return new LogisticComponent.PortKey(machineId, portIndex);
        }
    }

    private final List<PortInfo> visiblePorts;

    public LogisticWorkerSyncPacket() {
        this.visiblePorts = new ArrayList<>();
    }

    public LogisticWorkerSyncPacket(int containerId, int index, LogisticWorker be) {
        super(containerId, index);
        this.visiblePorts = be.getVisiblePorts();
    }

    public Collection<PortInfo> getPorts() {
        return visiblePorts;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        super.serializeToBuf(buf);
        buf.writeCollection(visiblePorts, PortInfo::serialize);
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        super.deserializeFromBuf(buf);
        visiblePorts.addAll(buf.readList(PortInfo::deserialize));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LogisticWorkerSyncPacket that)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        var thisPorts = new HashSet<>(visiblePorts);
        return visiblePorts.size() == that.visiblePorts.size() &&
            thisPorts.containsAll(that.visiblePorts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), visiblePorts);
    }
}
