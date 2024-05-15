package org.shsts.tinactory.core.common;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.content.AllCapabilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EventManager implements ICapabilityProvider {
    private final Multimap<Event<?>, Consumer<?>> handlers = HashMultimap.create();
    private final Multimap<ReturnEvent<?, ?>, ReturnEvent.Handler<?, ?>> returnHandlers = HashMultimap.create();

    public EventManager() {}

    public <A> void invoke(Event<A> event, A arg) {
        for (var handler : handlers.get(event)) {
            event.invoke(handler, arg);
        }
    }

    public <A, R> R invoke(ReturnEvent<A, R> event, A arg) {
        var token = event.newToken();
        for (var handler : returnHandlers.get(event)) {
            event.invoke(handler, arg, token);
        }
        return token.getReturn();
    }

    public <A> void subscribe(Supplier<Event<A>> event, Consumer<A> handler) {
        handlers.put(event.get(), handler);
    }

    public <A, R> void subscribe(Supplier<ReturnEvent<A, R>> event, ReturnEvent.Handler<A, R> handler) {
        returnHandlers.put(event.get(), handler);
    }

    public static <A> void invoke(BlockEntity be, Supplier<Event<A>> event, A arg) {
        be.getCapability(AllCapabilities.EVENT_MANAGER.get())
                .ifPresent(eventManager -> eventManager.invoke(event.get(), arg));
    }

    public static <A, R> R invokeReturn(BlockEntity be, Supplier<ReturnEvent<A, R>> event, A arg) {
        return be.getCapability(AllCapabilities.EVENT_MANAGER.get())
                .map(eventManager -> eventManager.invoke(event.get(), arg))
                .orElseThrow();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.EVENT_MANAGER.get()) {
            return LazyOptional.of(() -> this).cast();
        }
        return LazyOptional.empty();
    }
}
