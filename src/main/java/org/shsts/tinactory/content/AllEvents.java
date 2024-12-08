package org.shsts.tinactory.content;

import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.shsts.tinactory.core.common.Event;
import org.shsts.tinactory.core.common.ReturnEvent;
import org.shsts.tinactory.core.network.Network;
import org.shsts.tinactory.core.network.NetworkComponent;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import static org.shsts.tinactory.Tinactory._REGISTRATE;

public final class AllEvents {
    public record OnUseArg(Player player, InteractionHand hand, BlockHitResult hitResult) {}

    public static final RegistryEntry<Event<Level>> SERVER_LOAD;
    public static final RegistryEntry<Event<Level>> CLIENT_LOAD;
    public static final RegistryEntry<Event<Level>> REMOVED_IN_WORLD;
    public static final RegistryEntry<Event<Level>> REMOVED_BY_CHUNK;
    public static final RegistryEntry<Event<Level>> SERVER_TICK;
    public static final RegistryEntry<ReturnEvent<OnUseArg, InteractionResult>> SERVER_USE;

    public static final RegistryEntry<Event<Unit>> CONTAINER_CHANGE;
    public static final RegistryEntry<Event<Network>> CONNECT;
    public static final RegistryEntry<Event<NetworkComponent.SchedulingBuilder>> BUILD_SCHEDULING;
    public static final RegistryEntry<Event<Unit>> SET_MACHINE_CONFIG;

    static {
        SERVER_LOAD = _REGISTRATE.event("server_load");
        CLIENT_LOAD = _REGISTRATE.event("client_load");
        REMOVED_IN_WORLD = _REGISTRATE.event("removed_in_world");
        REMOVED_BY_CHUNK = _REGISTRATE.event("removed_by_chunk");
        SERVER_TICK = _REGISTRATE.event("server_tick");
        SERVER_USE = _REGISTRATE.returnEvent("server_use", InteractionResult.PASS);

        CONTAINER_CHANGE = _REGISTRATE.event("logistics/container_change");
        CONNECT = _REGISTRATE.event("network/connect");
        BUILD_SCHEDULING = _REGISTRATE.event("network/build_scheduling");
        SET_MACHINE_CONFIG = _REGISTRATE.event("machine/set_config");
    }

    public static void init() {}
}
