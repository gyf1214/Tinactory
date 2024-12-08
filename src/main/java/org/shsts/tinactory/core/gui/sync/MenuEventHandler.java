package org.shsts.tinactory.core.gui.sync;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.network.NetworkEvent;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket1;
import org.shsts.tinactory.core.gui.Menu;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MenuEventHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    public record Event<P extends MenuEventPacket>(int id, Class<P> clazz) {}

    public static final Event<SlotEventPacket1> FLUID_SLOT_CLICK;
    public static final Event<SetMachineConfigPacket1> SET_MACHINE_CONFIG;

    private static <P extends MenuEventPacket> void handle(P packet, NetworkEvent.Context ctx) {
        var player = ctx.getSender();
        if (player != null && player.containerMenu instanceof Menu<?, ?> menu &&
            menu.containerId == packet.getContainerId()) {
            menu.handleEventPacket(packet);
        }
    }

    private static final AtomicInteger EVENT_ID = new AtomicInteger(0);
    private static final Set<Class<? extends MenuEventPacket>> handledClasses = new HashSet<>();

    private static <P extends MenuEventPacket> Event<P> register(Class<P> clazz, Supplier<P> constructor) {
        var nextId = EVENT_ID.getAndIncrement();
        LOGGER.debug("register container event packet {} id={}", clazz.getSimpleName(), nextId);
        if (!handledClasses.contains(clazz)) {
            Tinactory.registryPacket(clazz, constructor, MenuEventHandler::handle);
            handledClasses.add(clazz);
        }
        return new Event<>(nextId, clazz);
    }

    static {
        FLUID_SLOT_CLICK = register(SlotEventPacket1.class, SlotEventPacket1::new);
        SET_MACHINE_CONFIG = register(SetMachineConfigPacket1.class, SetMachineConfigPacket1::new);
    }

    public static void init() {}
}
