package org.shsts.tinactory.core.network;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.IEventSubscriber;
import org.shsts.tinactory.core.common.ReturnEvent;
import org.shsts.tinactory.core.tech.TeamProfile;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;

import java.util.Optional;

import static org.shsts.tinactory.content.AllCapabilities.NETWORK_CONTROLLER;
import static org.shsts.tinactory.content.AllEvents.REMOVED_BY_CHUNK;
import static org.shsts.tinactory.content.AllEvents.REMOVED_IN_WORLD;
import static org.shsts.tinactory.content.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.content.AllEvents.SERVER_TICK;
import static org.shsts.tinactory.content.AllEvents.SERVER_USE;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NetworkController extends CapabilityProvider implements IEventSubscriber,
    INBTSerializable<CompoundTag> {
    private static final String ID = "network/controller";

    private final BlockEntity blockEntity;

    @Nullable
    private Network network;
    @Nullable
    private String teamName = null;

    public NetworkController(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public static <P> CapabilityProviderBuilder<BlockEntity, P> factory(P parent) {
        return CapabilityProviderBuilder.fromFactory(parent, ID, NetworkController::new);
    }

    private void createNetwork(TeamProfile team) {
        var world = blockEntity.getLevel();
        assert world != null && !world.isClientSide;
        assert network == null;
        network = new Network(world, blockEntity.getBlockPos(), team);
        teamName = network.team.getName();
        blockEntity.setChanged();
    }

    public Optional<Network> getNetwork() {
        return Optional.ofNullable(network);
    }

    private void onServerLoad() {
        if (teamName != null) {
            TechManager.server().teamByName(teamName).ifPresent(this::createNetwork);
        }
    }

    private void onServerTick() {
        if (network != null) {
            network.tick();
        }
    }

    private void onRemoved() {
        if (network != null) {
            network.destroy();
        }
    }

    private void initByPlayer(Player player) {
        if (network != null) {
            return;
        }
        TechManager.server().teamByPlayer(player).ifPresent(this::createNetwork);
    }

    public boolean canPlayerInteract(Player player) {
        initByPlayer(player);
        return network == null || network.team.hasPlayer(player);
    }

    private void onServerUse(AllEvents.OnUseArg args,
        ReturnEvent.Token<InteractionResult> result) {
        if (!canPlayerInteract(args.player())) {
            result.setReturn(InteractionResult.FAIL);
        }
    }

    @Override
    public void subscribeEvents(EventManager eventManager) {
        eventManager.subscribe(SERVER_LOAD, $ -> this.onServerLoad());
        eventManager.subscribe(REMOVED_IN_WORLD, $ -> this.onRemoved());
        eventManager.subscribe(REMOVED_BY_CHUNK, $ -> this.onRemoved());
        eventManager.subscribe(SERVER_TICK, $ -> this.onServerTick());
        eventManager.subscribe(SERVER_USE, this::onServerUse);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == NETWORK_CONTROLLER.get()) {
            return myself().cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        if (teamName != null) {
            tag.putString("team", teamName);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("team", Tag.TAG_STRING)) {
            teamName = tag.getString("team");
        }
    }
}
