package org.shsts.tinactory.core.gui.sync;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.core.gui.ContainerMenu;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
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
        LOGGER.debug("register container sync packet {}", clazz.getSimpleName());
        Tinactory.registryClientPacket(clazz, constructor, ContainerSyncHandler::handle);
    }

    public static void registerPackets() {
        register(ContainerSyncPacket.Boolean.class, ContainerSyncPacket.Boolean::new);
        register(ContainerSyncPacket.Long.class, ContainerSyncPacket.Long::new);
        register(ContainerSyncPacket.Double.class, ContainerSyncPacket.Double::new);
        register(ContainerSyncPacket.LocHolder.class, ContainerSyncPacket.LocHolder::new);
        register(FluidSyncPacket.class, FluidSyncPacket::new);
    }
}
