package org.shsts.tinactory.core.gui.sync;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraftforge.network.NetworkEvent;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.gui.sync.NetworkControllerSyncPacket;
import org.shsts.tinactory.core.gui.Menu;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MenuSyncHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static <P extends MenuSyncPacket>
    void handle(P packet, NetworkEvent.Context ctx) {
        var player = Minecraft.getInstance().player;
        if (player != null && player.containerMenu instanceof Menu<?, ?> menu &&
                menu.containerId == packet.getContainerId()) {
            menu.handleSyncPacket(packet.getIndex(), packet);
        }
    }

    private static <P extends MenuSyncPacket>
    void register(Class<P> clazz, Supplier<P> constructor) {
        LOGGER.debug("register container sync packet {}", clazz.getSimpleName());
        Tinactory.registryClientPacket(clazz, constructor, MenuSyncHandler::handle);
    }

    public static void init() {
        register(MenuSyncPacket.Boolean.class, MenuSyncPacket.Boolean::new);
        register(MenuSyncPacket.Long.class, MenuSyncPacket.Long::new);
        register(MenuSyncPacket.Double.class, MenuSyncPacket.Double::new);
        register(MenuSyncPacket.LocHolder.class, MenuSyncPacket.LocHolder::new);
        register(ChestItemSyncPacket.class, ChestItemSyncPacket::new);
        register(FluidSyncPacket.class, FluidSyncPacket::new);
        register(NetworkControllerSyncPacket.class, NetworkControllerSyncPacket::new);
    }
}
