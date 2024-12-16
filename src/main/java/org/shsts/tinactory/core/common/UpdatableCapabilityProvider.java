package org.shsts.tinactory.core.common;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinycorelib.api.blockentity.INBTUpdatable;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class UpdatableCapabilityProvider extends CapabilityProvider
    implements INBTUpdatable<CompoundTag> {
    private boolean isUpdateForced = true;

    protected void sendUpdate(BlockEntity be) {
        forceUpdate();
        var world = be.getLevel();
        assert world != null;
        be.setChanged();
        var pos = be.getBlockPos();
        var state = be.getBlockState();
        world.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
    }

    protected void forceUpdate() {
        isUpdateForced = true;
    }

    @Override
    public boolean shouldSendUpdate() {
        if (isUpdateForced) {
            isUpdateForced = false;
            return true;
        }
        return false;
    }

    @Override
    public abstract CompoundTag serializeOnUpdate();

    @Override
    public abstract void deserializeOnUpdate(CompoundTag tag);
}
