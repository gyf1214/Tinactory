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
import org.shsts.tinactory.core.tech.TeamProfile;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.blockentity.IReturnEvent;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import java.util.Optional;

import static org.shsts.tinactory.content.AllEvents.BLOCK_USE;
import static org.shsts.tinactory.content.AllEvents.REMOVED_BY_CHUNK;
import static org.shsts.tinactory.content.AllEvents.REMOVED_IN_WORLD;
import static org.shsts.tinactory.content.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.content.AllEvents.SERVER_TICK;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NetworkController extends CapabilityProvider
    implements IEventSubscriber, INBTSerializable<CompoundTag> {
    private static final String ID = "network/controller";

    private final BlockEntity blockEntity;

    @Nullable
    private Network network;
    @Nullable
    private String teamName = null;

    private NetworkController(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public static <P> IBlockEntityTypeBuilder<P> factory(
        IBlockEntityTypeBuilder<P> builder) {
        return builder.capability(ID, NetworkController::new);
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

    private void onRemoved() {}

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

    private void onUse(AllEvents.OnUseArg arg, IReturnEvent.Result<InteractionResult> result) {
        // TODO: unfortunately client does not know whether the player can interact with this machine,
        //       so on client we simply pass.
        if (arg.player().level.isClientSide) {
            return;
        }
        if (!canPlayerInteract(arg.player())) {
            result.set(InteractionResult.FAIL);
        }
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(SERVER_LOAD.get(), $ -> onServerLoad());
        eventManager.subscribe(REMOVED_IN_WORLD.get(), $ -> onRemoved());
        eventManager.subscribe(REMOVED_BY_CHUNK.get(), $ -> onRemoved());
        eventManager.subscribe(SERVER_TICK.get(), $ -> onServerTick());
        eventManager.subscribe(BLOCK_USE.get(), this::onUse);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
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

    public static Optional<NetworkController> tryGet(BlockEntity be) {
        return tryGetProvider(be, ID, NetworkController.class);
    }

    public static NetworkController get(BlockEntity be) {
        return getProvider(be, ID, NetworkController.class);
    }
}
