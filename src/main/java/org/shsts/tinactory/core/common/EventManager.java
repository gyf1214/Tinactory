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

    public EventManager() {}

    @SuppressWarnings("unchecked")
    public <A> void invoke(Event<A> event, A arg) {
        for (var handler : handlers.get(event)) {
            ((Consumer<A>) handler).accept(arg);
        }
    }

    public <A> void subscribe(Supplier<Event<A>> event, Consumer<A> handler) {
        handlers.put(event.get(), handler);
    }

    public static <A> void invoke(BlockEntity be, Supplier<Event<A>> event, A arg) {
        be.getCapability(AllCapabilities.EVENT_MANAGER.get())
                .ifPresent(eventManager -> eventManager.invoke(event.get(), arg));
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
