package org.shsts.tinactory.gui.sync;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.gui.ContainerMenu;
import org.slf4j.Logger;

import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public final class ContainerSyncHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static <P extends ContainerSyncPacket>
    void handle(P packet, NetworkEvent.Context ctx) {
        var player = Minecraft.getInstance().player;
        if (player != null && player.containerMenu instanceof ContainerMenu<?> menu &&
                menu.containerId == packet.containerId) {
            menu.onSyncPacket(packet.index, packet);
        }
    }

    private static <P extends ContainerSyncPacket>
    void register(Class<P> clazz, Function<FriendlyByteBuf, P> factory) {
        LOGGER.debug("register container sync packet {}", clazz);
        Tinactory.registryClientPacket(clazz, factory, ContainerSyncHandler::handle);
    }

    public static void registerPackets() {
        register(ContainerSyncPacket.Long.class, ContainerSyncPacket.Long::create);
        register(ContainerSyncPacket.Double.class, ContainerSyncPacket.Double::create);
    }
}
