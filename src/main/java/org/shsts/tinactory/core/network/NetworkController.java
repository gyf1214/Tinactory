package org.shsts.tinactory.core.network;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.tech.TeamProfile;
import org.shsts.tinactory.core.tech.TechManager;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NetworkController extends SmartBlockEntity {
    @Nullable
    private Network network;
    @Nullable
    private String teamName = null;

    public NetworkController(BlockEntityType<NetworkController> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private void createNetwork(TeamProfile team) {
        assert level != null && !level.isClientSide;
        assert network == null;
        network = new Network(level, worldPosition, team);
        teamName = network.team.getName();
        setChanged();
    }

    public Optional<Network> getNetwork() {
        return Optional.ofNullable(network);
    }

    @Override
    protected void onServerLoad(Level world) {
        super.onServerLoad(world);
        if (teamName != null) {
            TechManager.server().teamByName(teamName).ifPresent(this::createNetwork);
        }
    }

    @Override
    protected void onServerTick(Level world, BlockPos pos, BlockState state) {
        if (network != null) {
            network.tick();
        }
    }

    private void onRemoved() {
        if (network != null) {
            network.destroy();
        }
    }

    @Override
    protected void onRemovedInWorld(Level world) {
        onRemoved();
        super.onRemovedInWorld(world);
    }

    @Override
    protected void onRemovedByChunk(Level world) {
        onRemoved();
        super.onRemovedByChunk(world);
    }

    public void initByPlayer(Player player) {
        if (network != null) {
            return;
        }
        TechManager.server().teamByPlayer(player).ifPresent(this::createNetwork);
    }

    public boolean canPlayerInteract(Player player) {
        initByPlayer(player);
        return network == null || network.team.hasPlayer(player);
    }

    @Override
    protected InteractionResult onServerUse(Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!canPlayerInteract(player)) {
            return InteractionResult.FAIL;
        }
        return super.onServerUse(player, hand, hitResult);
    }

    @Override
    protected void serializeOnSave(CompoundTag tag) {
        super.serializeOnSave(tag);
        if (teamName != null) {
            tag.putString("team", teamName);
        }
    }

    @Override
    protected void deserializeOnSave(CompoundTag tag) {
        super.deserializeOnSave(tag);
        if (tag.contains("team", Tag.TAG_STRING)) {
            teamName = tag.getString("team");
        }
    }
}
