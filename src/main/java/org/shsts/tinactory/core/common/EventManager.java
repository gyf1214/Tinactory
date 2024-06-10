package org.shsts.tinactory.core.common;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.content.AllCapabilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EventManager extends CapabilityProvider {
    public static final ResourceLocation LOC = modLoc("event_manager");

    private final Multimap<Event<?>, Consumer<?>> handlers = HashMultimap.create();
    private final Multimap<ReturnEvent<?, ?>, ReturnEvent.Handler<?, ?>> returnHandlers = HashMultimap.create();

    public EventManager() {}

    public <A> void invoke(Event<A> event, A arg) {
        for (var handler : handlers.get(event)) {
            event.invoke(handler, arg);
        }
    }

    public void invoke(Event<Unit> event) {
        invoke(event, Unit.INSTANCE);
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

    public void subscribe(Supplier<Event<Unit>> event, Runnable handler) {
        subscribe(event, $ -> handler.run());
    }

    public <A, R> void subscribe(Supplier<ReturnEvent<A, R>> event, ReturnEvent.Handler<A, R> handler) {
        returnHandlers.put(event.get(), handler);
    }

    public static <A> void invoke(BlockEntity be, Supplier<Event<A>> event, A arg) {
        AllCapabilities.EVENT_MANAGER.tryGet(be)
                .ifPresent(eventManager -> eventManager.invoke(event.get(), arg));
    }

    public static void invoke(BlockEntity be, Supplier<Event<Unit>> event) {
        AllCapabilities.EVENT_MANAGER.tryGet(be)
                .ifPresent(eventManager -> eventManager.invoke(event.get()));
    }

    public static <A, R> R invokeReturn(BlockEntity be, Supplier<ReturnEvent<A, R>> event, A arg) {
        var e = event.get();
        return AllCapabilities.EVENT_MANAGER.tryGet(be)
                .map(eventManager -> eventManager.invoke(e, arg))
                .orElse(e.getDefaultReturn());
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.EVENT_MANAGER.get()) {
            return myself();
        }
        return LazyOptional.empty();
    }
}
