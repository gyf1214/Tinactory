package org.shsts.tinactory.registrate.common;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.NoSuchElementException;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CapabilityEntry<T> extends RegistryEntry<Capability<T>> {
    public CapabilityEntry(String modid, CapabilityToken<T> token) {
        // ignore id
        super(modid, "", () -> CapabilityManager.get(token));
    }

    public T getCapability(BlockEntity blockEntity) {
        return this.getCapability(blockEntity, null);
    }

    public T getCapability(BlockEntity blockEntity, @Nullable Direction dir) {
        return blockEntity.getCapability(this.get(), dir)
                .orElseThrow(NoSuchElementException::new);
    }
}
