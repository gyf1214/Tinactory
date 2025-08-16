package org.shsts.tinactory.core.common;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinycorelib.api.blockentity.IEvent;
import org.shsts.tinycorelib.api.blockentity.IReturnEvent;

import java.util.Optional;
import java.util.function.Supplier;

import static org.shsts.tinactory.content.AllCapabilities.EVENT_MANAGER;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class CapabilityProvider implements ICapabilityProvider {
    private final LazyOptional<?> myself = LazyOptional.of(() -> this);

    protected <T> LazyOptional<T> myself() {
        return myself.cast();
    }

    public static void invoke(BlockEntity be, Supplier<IEvent<Unit>> event) {
        EVENT_MANAGER.tryGet(be).ifPresent($ -> $.invoke(event.get()));
    }

    public static <A> void invoke(BlockEntity be, Supplier<IEvent<A>> event, A arg) {
        EVENT_MANAGER.tryGet(be).ifPresent($ -> $.invoke(event.get(), arg));
    }

    public static <A, R> R invokeReturn(BlockEntity be, Supplier<IReturnEvent<A, R>> event,
        A arg) {
        return EVENT_MANAGER.tryGet(be)
            .map($ -> $.invokeReturn(event.get(), arg))
            .orElse(event.get().defaultResult());
    }

    public static <T extends ICapabilityProvider> Optional<T> tryGetProvider(
        BlockEntity be, String id, Class<T> clazz) {
        return EVENT_MANAGER.tryGet(be)
            .flatMap($ -> $.tryGetProvider(modLoc(id), clazz));
    }

    public static <T extends ICapabilityProvider> T getProvider(
        BlockEntity be, String id, Class<T> clazz) {
        return EVENT_MANAGER.get(be).getProvider(modLoc(id), clazz);
    }
}
