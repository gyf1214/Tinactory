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
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.core.tech.TeamProfile;
import org.shsts.tinactory.core.tech.TechManager;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NetworkController extends Machine {
    @Nullable
    private Network network = null;
    @Nullable
    private String teamName = null;

    public NetworkController(BlockEntityType<NetworkController> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private void createNetwork(TeamProfile team) {
        assert level != null && !level.isClientSide;
        assert network == null;
        network = new Network(level, worldPosition, team);
    }

    @Override
    protected void onServerLoad(Level world) {
        super.onServerLoad(world);
        if (teamName != null) {
            TechManager.teamByName(teamName).ifPresent(this::createNetwork);
            teamName = null;
        }
    }

    @Override
    protected void onServerTick(Level world, BlockPos pos, BlockState state) {
        if (network != null) {
            network.tick();
        }
    }

    @Override
    protected void onRemovedInWorld(Level world) {
        if (network != null) {
            network.destroy();
        }
        super.onRemovedInWorld(world);
    }

    @Override
    protected void onRemovedByChunk(Level world) {
        if (network != null) {
            network.destroy();
        }
        super.onRemovedByChunk(world);
    }

    @Override
    protected InteractionResult onServerUse(Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (network != null) {
            return InteractionResult.PASS;
        }
        TechManager.teamByPlayer(player).ifPresent(this::createNetwork);
        return InteractionResult.PASS;
    }

    @Override
    protected void serializeOnSave(CompoundTag tag) {
        super.serializeOnSave(tag);
        if (network != null) {
            tag.putString("team", network.team.getName());
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
