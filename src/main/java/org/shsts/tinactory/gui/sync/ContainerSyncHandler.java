package org.shsts.tinactory.gui.sync;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.gui.ContainerMenu;
import org.slf4j.Logger;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public final class ContainerSyncHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static <P extends ContainerSyncPacket>
    void handle(P packet, NetworkEvent.Context ctx) {
        var player = Minecraft.getInstance().player;
        if (player != null && player.containerMenu instanceof ContainerMenu<?> menu &&
                menu.containerId == packet.getContainerId()) {
            menu.onSyncPacket(packet.getIndex(), packet);
        }
    }

    private static <P extends ContainerSyncPacket>
    void register(Class<P> clazz, Supplier<P> constructor) {
        LOGGER.debug("register container sync packet {}", clazz);
        Tinactory.registryClientPacket(clazz, buf -> {
            var p = constructor.get();
            p.deserializeFromBuf(buf);
            return p;
        }, ContainerSyncHandler::handle);
    }

    public static void registerPackets() {
        register(ContainerSyncPacket.Long.class, ContainerSyncPacket.Long::new);
        register(ContainerSyncPacket.Double.class, ContainerSyncPacket.Double::new);
        register(FluidSyncPacket.class, FluidSyncPacket::new);
    }
}
