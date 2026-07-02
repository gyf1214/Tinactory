package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MESignalControllerSyncPacket implements IPacket {
    public record SignalInfo(UUID machineId, Component machineName, ItemStack icon,
        String key, boolean isWrite) {
        private static void serialize(RegistryFriendlyByteBuf buf, SignalInfo info) {
            buf.writeUUID(info.machineId);
            CodecHelper.encodeComponentToBuf(buf, info.machineName);
            StackHelper.serializeStackToBuf(buf, info.icon);
            buf.writeUtf(info.key);
            buf.writeBoolean(info.isWrite);
        }

        public static SignalInfo deserialize(RegistryFriendlyByteBuf buf) {
            return new SignalInfo(buf.readUUID(),
                CodecHelper.parseComponentFromBuf(buf),
                StackHelper.deserializeStackFromBuf(buf),
                buf.readUtf(), buf.readBoolean());
        }
    }

    private final List<SignalInfo> visibleSignals;

    public MESignalControllerSyncPacket() {
        this.visibleSignals = new ArrayList<>();
    }

    public MESignalControllerSyncPacket(List<SignalInfo> visibleSignals) {
        this.visibleSignals = visibleSignals;
    }

    public List<SignalInfo> signals() {
        return visibleSignals;
    }

    @Override
    public void serializeToBuf(RegistryFriendlyByteBuf buf) {
        buf.writeCollection(visibleSignals, SignalInfo::serialize);
    }

    @Override
    public void deserializeFromBuf(RegistryFriendlyByteBuf buf) {
        visibleSignals.addAll(buf.readList(SignalInfo::deserialize));
    }
}
