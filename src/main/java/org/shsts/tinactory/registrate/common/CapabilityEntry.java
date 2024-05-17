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
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CapabilityEntry<T> extends RegistryEntry<Capability<T>> {
    public CapabilityEntry(String modid, CapabilityToken<T> token) {
        // ignore id
        super(modid, "", () -> CapabilityManager.get(token));
    }

    public T get(BlockEntity be) {
        return get(be, null);
    }

    public T get(BlockEntity be, @Nullable Direction dir) {
        return be.getCapability(get(), dir)
                .orElseThrow(NoSuchElementException::new);
    }

    public Optional<T> tryGet(BlockEntity be) {
        return tryGet(be, null);
    }

    public Optional<T> tryGet(BlockEntity be, @Nullable Direction dir) {
        return be.getCapability(get(), dir).resolve();
    }
}
