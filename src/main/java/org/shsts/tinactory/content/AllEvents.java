package org.shsts.tinactory.content;

import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.shsts.tinactory.core.network.Network;
import org.shsts.tinactory.core.network.NetworkComponent;
import org.shsts.tinycorelib.api.blockentity.IEvent;
import org.shsts.tinycorelib.api.blockentity.IReturnEvent;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.handler.IEntryHandler;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinycorelib.api.CoreLibKeys.CLIENT_LOAD_LOC;
import static org.shsts.tinycorelib.api.CoreLibKeys.EVENT_REGISTRY_KEY;
import static org.shsts.tinycorelib.api.CoreLibKeys.REMOVED_BY_CHUNK_LOC;
import static org.shsts.tinycorelib.api.CoreLibKeys.REMOVED_IN_WORLD_LOC;
import static org.shsts.tinycorelib.api.CoreLibKeys.SERVER_LOAD_LOC;
import static org.shsts.tinycorelib.api.CoreLibKeys.SERVER_TICK_LOC;

public final class AllEvents {
    public static final IEntryHandler<IEvent<?>> EVENTS;
    public static final IEntry<IEvent<Level>> SERVER_LOAD;
    public static final IEntry<IEvent<Level>> CLIENT_LOAD;
    public static final IEntry<IEvent<Level>> REMOVED_IN_WORLD;
    public static final IEntry<IEvent<Level>> REMOVED_BY_CHUNK;
    public static final IEntry<IEvent<Level>> SERVER_TICK;

    public record OnUseArg(Player player, InteractionHand hand, BlockHitResult hitResult) {}

    public static final IEntry<IReturnEvent<AllEvents.OnUseArg, InteractionResult>> SERVER_USE;
    public static final IEntry<IEvent<Unit>> CONTAINER_CHANGE;
    public static final IEntry<IEvent<Network>> CONNECT;
    public static final IEntry<IEvent<NetworkComponent.SchedulingBuilder>> BUILD_SCHEDULING;
    public static final IEntry<IEvent<Unit>> SET_MACHINE_CONFIG;

    private AllEvents() {}

    static {
        EVENTS = REGISTRATE.getHandler(EVENT_REGISTRY_KEY, IEvent.class);

        SERVER_LOAD = EVENTS.getEntry(SERVER_LOAD_LOC);
        CLIENT_LOAD = EVENTS.getEntry(CLIENT_LOAD_LOC);
        REMOVED_IN_WORLD = EVENTS.getEntry(REMOVED_IN_WORLD_LOC);
        REMOVED_BY_CHUNK = EVENTS.getEntry(REMOVED_BY_CHUNK_LOC);
        SERVER_TICK = EVENTS.getEntry(SERVER_TICK_LOC);

        SERVER_USE = REGISTRATE.returnEvent("server_use", InteractionResult.PASS);
        CONTAINER_CHANGE = REGISTRATE.event("logistics/container_change");
        CONNECT = REGISTRATE.event("network/connect");
        BUILD_SCHEDULING = REGISTRATE.event("network/build_scheduling");
        SET_MACHINE_CONFIG = REGISTRATE.event("machine/set_config");
    }

    public static void init() {}
}
