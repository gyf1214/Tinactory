package org.shsts.tinactory.core;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * workaround to <a href="https://github.com/MinecraftForge/MinecraftForge/issues/8302">MinecraftForge#8302</a>
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SmartBlockEntity extends BlockEntity {
    protected boolean isChunkUnloaded = false;
    protected boolean isUpdateForced = false;

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
        super.setRemoved();
        assert level != null;
        if (!isChunkUnloaded) {
            onRemovedInWorld(level);
        } else {
            onRemovedByChunk(level);
        }
    }

    public static <T extends BlockEntity> void ticker(Level world, BlockPos pos, BlockState state, T be) {
        if (be instanceof SmartBlockEntity sbe) {
            if (world.isClientSide()) {
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
        onLoad(level);
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

    @Override
    public CompoundTag getUpdateTag() {
        var tag = new CompoundTag();
        serializeOnUpdate(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return shouldSendUpdate() ? ClientboundBlockEntityDataPacket.create(this) : null;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        deserializeOnUpdate(tag);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        var tag = pkt.getTag();
        if (tag != null) {
            deserializeOnUpdate(tag);
        }
    }

    protected void notifyUpdate() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            isUpdateForced = true;
            var state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 2);
        }
    }

    /**
     * callback when this blockEntity is loaded
     */
    protected void onLoad(Level world) {}

    /**
     * callback when this blockEntity is truly removed in world
     */
    protected void onRemovedInWorld(Level world) {}

    /**
     * callback when this blockEntity is removed because of chunk unload
     */
    protected void onRemovedByChunk(Level world) {}

    /**
     * Sever tick callback, need the block to have ticking = true
     */
    protected void onServerTick(Level world, BlockPos pos, BlockState state) {}

    /**
     * Client tick callback, need the block to have ticking = true
     */
    protected void onClientTick(Level world, BlockPos pos, BlockState state) {}

    protected void serializeOnSave(CompoundTag tag) {}

    protected void deserializeOnSave(CompoundTag tag) {}

    protected boolean shouldSendUpdate() {
        if (isUpdateForced) {
            isUpdateForced = false;
            return true;
        }
        return false;
    }

    protected void serializeOnUpdate(CompoundTag tag) {
        serializeOnSave(tag);
    }

    protected void deserializeOnUpdate(CompoundTag tag) {
        deserializeOnSave(tag);
    }
}
