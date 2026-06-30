package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LogisticWorkerSyncPacket implements IPacket {
    public record PortInfo(UUID machineId, int portIndex, Component machineName,
        ItemStack icon, Component portName) {
        public static void serialize(FriendlyByteBuf buf, PortInfo info) {
            buf.writeUUID(info.machineId);
            buf.writeVarInt(info.portIndex);
            buf.writeUtf(CodecHelper.encodeComponent(info.machineName));
            StackHelper.serializeStackToBuf((RegistryFriendlyByteBuf) buf, info.icon);
            buf.writeUtf(CodecHelper.encodeComponent(info.portName));
        }

        public static PortInfo deserialize(FriendlyByteBuf buf) {
            return new PortInfo(buf.readUUID(), buf.readVarInt(),
                CodecHelper.parseComponent(buf.readUtf()),
                StackHelper.deserializeStackFromBuf((RegistryFriendlyByteBuf) buf),
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

    public LogisticWorkerSyncPacket(List<PortInfo> visiblePorts) {
        this.visiblePorts = visiblePorts;
    }

    public Collection<PortInfo> ports() {
        return visiblePorts;
    }

    @Override
    public void serializeToBuf(RegistryFriendlyByteBuf buf) {
        buf.writeCollection(visiblePorts, PortInfo::serialize);
    }

    @Override
    public void deserializeFromBuf(RegistryFriendlyByteBuf buf) {
        visiblePorts.addAll(buf.readList(PortInfo::deserialize));
    }
}
