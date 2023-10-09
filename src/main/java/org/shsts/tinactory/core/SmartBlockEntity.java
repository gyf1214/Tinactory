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
        this.isChunkUnloaded = true;
    }

    @Override
    public final void setRemoved() {
        super.setRemoved();
        assert this.level != null;
        if (!this.isChunkUnloaded) {
            onRemovedInWorld(this.level);
        } else {
            onRemovedByChunk(this.level);
        }
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
        assert this.level != null;
        this.onLoad(this.level);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.serializeOnSave(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.deserializeOnSave(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        var tag = new CompoundTag();
        this.serializeOnUpdate(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return this.shouldSendUpdate() ? ClientboundBlockEntityDataPacket.create(this) : null;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.deserializeOnUpdate(tag);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        var tag = pkt.getTag();
        if (tag != null) {
            this.deserializeOnUpdate(tag);
        }
    }

    protected void notifyUpdate() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.isUpdateForced = true;
            var state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, 2);
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
        if (this.isUpdateForced) {
            this.isUpdateForced = false;
            return true;
        }
        return false;
    }

    protected void serializeOnUpdate(CompoundTag tag) {
        this.serializeOnSave(tag);
    }

    protected void deserializeOnUpdate(CompoundTag tag) {
        this.deserializeOnSave(tag);
    }
}
