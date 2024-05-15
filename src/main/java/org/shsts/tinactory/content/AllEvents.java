package org.shsts.tinactory.content;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.shsts.tinactory.content.gui.sync.SetMachinePacket;
import org.shsts.tinactory.content.logistics.LogisticsComponent;
import org.shsts.tinactory.core.common.Event;
import org.shsts.tinactory.core.common.ReturnEvent;
import org.shsts.tinactory.core.network.Component;
import org.shsts.tinactory.core.network.Network;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

public final class AllEvents {
    public record OnUseArg(Player player, InteractionHand hand, BlockHitResult hitResult) {}

    public static final RegistryEntry<Event<Level>> SERVER_LOAD;
    public static final RegistryEntry<Event<Level>> REMOVED_IN_WORLD;
    public static final RegistryEntry<Event<Level>> REMOVED_BY_CHUNK;
    public static final RegistryEntry<Event<Level>> SERVER_TICK;
    public static final RegistryEntry<ReturnEvent<OnUseArg, InteractionResult>> SERVER_USE;

    public static final RegistryEntry<Event<Boolean>> CONTAINER_CHANGE;
    public static final RegistryEntry<Event<Network>> CONNECT;
    public static final RegistryEntry<Event<Component.SchedulingBuilder>> BUILD_SCHEDULING;
    public static final RegistryEntry<Event<LogisticsComponent>> DUMP_ITEM_OUTPUT;
    public static final RegistryEntry<Event<LogisticsComponent>> DUMP_FLUID_OUTPUT;
    public static final RegistryEntry<Event<SetMachinePacket>> SET_MACHINE_CONFIG;

    static {
        SERVER_LOAD = REGISTRATE.event("on_server_load");
        REMOVED_IN_WORLD = REGISTRATE.event("on_removed_in_world");
        REMOVED_BY_CHUNK = REGISTRATE.event("on_removed_by_chunk");
        SERVER_TICK = REGISTRATE.event("on_server_tick");
        SERVER_USE = REGISTRATE.returnEvent("on_server_use", InteractionResult.PASS);

        CONTAINER_CHANGE = REGISTRATE.event("logistics/container_change");
        CONNECT = REGISTRATE.event("network/connect");
        BUILD_SCHEDULING = REGISTRATE.event("network/build_scheduling");
        DUMP_ITEM_OUTPUT = REGISTRATE.event("logistics/dump_item_output");
        DUMP_FLUID_OUTPUT = REGISTRATE.event("logistics/dump_fluid_output");
        SET_MACHINE_CONFIG = REGISTRATE.event("machine/set_config");
    }

    public static void init() {}
}
