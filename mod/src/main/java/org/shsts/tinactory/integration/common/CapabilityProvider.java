package org.shsts.tinactory.integration.common;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinycorelib.api.blockentity.ICapabilityBuilder;
import org.shsts.tinycorelib.api.blockentity.ICapabilityContainer;
import org.shsts.tinycorelib.api.blockentity.IEvent;
import org.shsts.tinycorelib.api.blockentity.IReturnEvent;

import java.util.Optional;
import java.util.function.Supplier;

import static org.shsts.tinactory.AllCapabilities.EVENT_MANAGER;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class CapabilityProvider implements ICapabilityContainer {
    @Override
    public void attachCapability(ICapabilityBuilder builder) {}

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

    public static <T extends ICapabilityContainer> Optional<T> tryGetContainer(
        BlockEntity be, String id, Class<T> clazz) {
        return EVENT_MANAGER.tryGet(be)
            .flatMap($ -> $.tryGetContainer(modLoc(id), clazz));
    }

    public static <T extends ICapabilityContainer> T getContainer(
        BlockEntity be, String id, Class<T> clazz) {
        return EVENT_MANAGER.get(be).getContainer(modLoc(id), clazz);
    }
}
