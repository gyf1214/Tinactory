package org.shsts.tinactory.content.sound;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import static org.shsts.tinactory.integration.network.MachineBlock.WORKING;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineSoundInstance extends AbstractTickableSoundInstance {
    private final BlockEntity blockEntity;

    public MachineSoundInstance(BlockEntity blockEntity, SoundEvent sound) {
        super(sound, SoundSource.BLOCKS);
        this.blockEntity = blockEntity;
        var pos = blockEntity.getBlockPos();
        this.looping = true;
        this.volume = 0.5f;
        this.pitch = 1f;
        this.x = pos.getX() + 0.5d;
        this.y = pos.getY() + 0.5d;
        this.z = pos.getZ() + 0.5d;
    }

    @Override
    public void tick() {
        if (shouldStop()) {
            stop();
        }
    }

    private boolean shouldStop() {
        if (blockEntity.isRemoved()) {
            return true;
        }
        var world = blockEntity.getLevel();
        if (world == null) {
            return true;
        }
        var pos = blockEntity.getBlockPos();
        if (!world.isLoaded(pos)) {
            return true;
        }
        var state = world.getBlockState(pos);
        return !state.hasProperty(WORKING) || !state.getValue(WORKING);
    }
}
