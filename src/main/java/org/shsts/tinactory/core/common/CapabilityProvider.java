package org.shsts.tinactory.core.common;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinycorelib.api.blockentity.IEvent;
import org.shsts.tinycorelib.api.blockentity.IReturnEvent;

import java.util.function.Supplier;

import static org.shsts.tinactory.content.AllCapabilities.EVENT_MANAGER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class CapabilityProvider implements ICapabilityProvider {
    private final LazyOptional<?> myself = LazyOptional.of(() -> this);

    protected <T> LazyOptional<T> myself() {
        return myself.cast();
    }

    protected static void invoke(BlockEntity be, Supplier<IEvent<Unit>> event) {
        EVENT_MANAGER.tryGet(be).ifPresent($ -> $.invoke(event.get()));
    }

    protected static <A> void invoke(BlockEntity be, Supplier<IEvent<A>> event, A arg) {
        EVENT_MANAGER.tryGet(be).ifPresent($ -> $.invoke(event.get(), arg));
    }

    protected static <A, R> R invokeReturn(BlockEntity be, Supplier<IReturnEvent<A, R>> event,
        A arg) {
        return EVENT_MANAGER.tryGet(be)
            .map($ -> $.invokeReturn(event.get(), arg))
            .orElse(event.get().defaultResult());
    }
}
