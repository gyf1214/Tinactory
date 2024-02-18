package org.shsts.tinactory.core.gui.sync;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.network.NetworkEvent;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.core.gui.ContainerMenu;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class ContainerEventHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    public record Event<P extends ContainerEventPacket>(int id, Class<P> clazz) {}

    public static final Event<FluidEventPacket> FLUID_CLICK;

    private static <P extends ContainerEventPacket> void handle(P packet, NetworkEvent.Context ctx) {
        var player = ctx.getSender();
        if (player != null && player.containerMenu instanceof ContainerMenu<?> menu &&
                menu.containerId == packet.getContainerId()) {
            menu.onEventPacket(packet);
        }
    }

    private static final AtomicInteger eventId = new AtomicInteger(0);
    private static final Set<Class<? extends ContainerEventPacket>> handledClasses = new HashSet<>();

    private static <P extends ContainerEventPacket>
    Event<P> register(Class<P> clazz, Supplier<P> constructor) {
        var nextId = eventId.getAndIncrement();
        LOGGER.debug("register container event packet {} id={}", clazz, nextId);
        if (!handledClasses.contains(clazz)) {
            Tinactory.registryPacket(clazz, constructor, ContainerEventHandler::handle);
            handledClasses.add(clazz);
        }
        return new Event<>(nextId, clazz);
    }

    static {
        FLUID_CLICK = register(FluidEventPacket.class, FluidEventPacket::new);
    }

    public static void init() {}
}
