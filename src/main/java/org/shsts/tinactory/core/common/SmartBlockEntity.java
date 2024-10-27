package org.shsts.tinactory.core.common;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.CapabilityItemHandler;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinactory.core.logistics.ItemHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

/**
 * workaround to <a href="https://github.com/MinecraftForge/MinecraftForge/issues/8302">MinecraftForge#8302</a>
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SmartBlockEntity extends BlockEntity {
    private boolean isChunkUnloaded = false;
    private boolean isUpdateForced = true;
    @Nullable
    private UpdateHelper updateHelper = null;

    public SmartBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        isChunkUnloaded = true;
    }

    @Override
    public final void setRemoved() {
        assert level != null;
        if (!isChunkUnloaded) {
            onRemovedInWorld(level);
        } else {
            onRemovedByChunk(level);
        }
        super.setRemoved();
    }

    /**
     * deal with cases when blockState changes when this function is called.
     */
    public Optional<BlockState> getRealBlockState() {
        assert level != null;
        if (!level.isLoaded(worldPosition)) {
            return Optional.empty();
        }
        var state = getBlockState();
        if (!level.getBlockState(worldPosition).is(state.getBlock())) {
            return Optional.empty();
        }
        return Optional.of(state);
    }

    public static <T extends BlockEntity> void ticker(Level world, BlockPos pos, BlockState state, T be) {
        if (be instanceof SmartBlockEntity sbe) {
            if (world.isClientSide) {
                sbe.onClientTick(world, pos, state);
            } else {
                sbe.onServerTick(world, pos, state);
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        assert level != null;
        if (level.isClientSide) {
            onClientLoad(level);
        } else {
            onServerLoad(level);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        serializeOnSave(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        deserializeOnSave(tag);
    }

    private UpdateHelper getUpdateHelper() {
        if (updateHelper == null) {
            updateHelper = AllCapabilities.UPDATE_HELPER.get(this);
        }
        return updateHelper;
    }

    public void sendUpdate() {
        assert level != null && !level.isClientSide;
        setChanged();
        var state = getBlockState();
        level.sendBlockUpdated(getBlockPos(), state, state, Block.UPDATE_CLIENTS);
    }

    private CompoundTag getUpdateTag(boolean forceUpdate) {
        var tag = new CompoundTag();
        var caps = getUpdateHelper().getUpdateTag(forceUpdate);
        tag.put("ForgeCaps", caps);
        if (forceUpdate || isUpdateForced) {
            serializeOnUpdate(tag);
        }
        return tag;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getUpdateTag(true);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        if (shouldSendUpdate()) {
            var ret = ClientboundBlockEntityDataPacket.create(this, be ->
                    ((SmartBlockEntity) be).getUpdateTag(false));
            resetShouldSendUpdate();
            return ret;
        }
        return null;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        var caps = tag.getList("ForgeCaps", Tag.TAG_COMPOUND);
        getUpdateHelper().handleUpdateTag(caps);
        deserializeOnUpdate(tag);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        var tag = pkt.getTag();
        if (tag != null) {
            handleUpdateTag(tag);
        }
    }

    /**
     * callback when this blockEntity is loaded
     */
    protected void onServerLoad(Level world) {
        EventManager.invoke(this, AllEvents.SERVER_LOAD, world);
    }

    protected void onClientLoad(Level world) {
        EventManager.invoke(this, AllEvents.CLIENT_LOAD, world);
    }

    /**
     * callback when this blockEntity is truly removed in world
     */
    protected void onRemovedInWorld(Level world) {
        EventManager.invoke(this, AllEvents.REMOVED_IN_WORLD, world);
        getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .ifPresent(itemHandler -> ItemHelper.dropItemHandler(world, worldPosition, itemHandler));
    }

    /**
     * callback when this blockEntity is removed because of chunk unload
     */
    protected void onRemovedByChunk(Level world) {
        EventManager.invoke(this, AllEvents.REMOVED_BY_CHUNK, world);
    }

    /**
     * Sever tick callback, need the block to have ticking = true
     */
    protected void onServerTick(Level world, BlockPos pos, BlockState state) {
        EventManager.invoke(this, AllEvents.SERVER_TICK, world);
    }

    /**
     * Client tick callback, need the block to have ticking = true
     */
    protected void onClientTick(Level world, BlockPos pos, BlockState state) {}

    public InteractionResult onUse(Player player, InteractionHand hand, BlockHitResult hitResult) {
        assert level != null;
        if (level.isClientSide) {
            return onClientUse(player, hand, hitResult);
        } else {
            return onServerUse(player, hand, hitResult);
        }
    }

    protected InteractionResult onServerUse(Player player, InteractionHand hand, BlockHitResult hitResult) {
        var arg = new AllEvents.OnUseArg(player, hand, hitResult);
        return EventManager.invokeReturn(this, AllEvents.SERVER_USE, arg);
    }

    protected InteractionResult onClientUse(Player player, InteractionHand hand, BlockHitResult hitResult) {
        return InteractionResult.PASS;
    }

    protected void serializeOnSave(CompoundTag tag) {}

    protected void deserializeOnSave(CompoundTag tag) {}

    private boolean shouldSendUpdate() {
        return getUpdateHelper().shouldSendUpdate() || isUpdateForced;
    }

    private void resetShouldSendUpdate() {
        getUpdateHelper().resetShouldSendUpdate();
        isUpdateForced = false;
    }

    protected void serializeOnUpdate(CompoundTag tag) {}

    protected void deserializeOnUpdate(CompoundTag tag) {}

    @Override
    public String toString() {
        return "%s(%s)@%s:%s".formatted(getClass().getSimpleName(),
                getType().getRegistryName(), level, worldPosition);
    }
}
