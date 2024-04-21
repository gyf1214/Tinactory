package org.shsts.tinactory.content;

import net.minecraft.world.level.Level;
import org.shsts.tinactory.content.gui.sync.SetMachinePacket;
import org.shsts.tinactory.content.logistics.LogisticsComponent;
import org.shsts.tinactory.core.common.Event;
import org.shsts.tinactory.core.network.Component;
import org.shsts.tinactory.core.network.Network;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

public final class AllBlockEntityEvents {
    public static final RegistryEntry<Event<Level>> SERVER_LOAD;
    public static final RegistryEntry<Event<Boolean>> CONTAINER_CHANGE;
    public static final RegistryEntry<Event<Network>> CONNECT;
    public static final RegistryEntry<Event<Component.SchedulingBuilder>> BUILD_SCHEDULING;
    public static final RegistryEntry<Event<LogisticsComponent>> DUMP_ITEM_OUTPUT;
    public static final RegistryEntry<Event<LogisticsComponent>> DUMP_FLUID_OUTPUT;
    public static final RegistryEntry<Event<SetMachinePacket>> SET_MACHINE_CONFIG;

    static {
        SERVER_LOAD = REGISTRATE.event("on_server_load", Level.class);
        CONTAINER_CHANGE = REGISTRATE.event("logistics/container_change", Boolean.class);
        CONNECT = REGISTRATE.event("network/connect", Network.class);
        BUILD_SCHEDULING = REGISTRATE.event("network/build_scheduling", Component.SchedulingBuilder.class);
        DUMP_ITEM_OUTPUT = REGISTRATE.event("logistics/dump_item_output", LogisticsComponent.class);
        DUMP_FLUID_OUTPUT = REGISTRATE.event("logistics/dump_fluid_output", LogisticsComponent.class);
        SET_MACHINE_CONFIG = REGISTRATE.event("machine/set_config", SetMachinePacket.class);
    }

    public static void init() {}
}
