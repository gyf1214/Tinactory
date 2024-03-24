package org.shsts.tinactory.core.common;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.content.AllCapabilities;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EventManager implements ICapabilityProvider {
    private final Multimap<Event<?>, Consumer<?>> handlers = HashMultimap.create();

    @SuppressWarnings("unchecked")
    public <A> void invoke(Event<A> event, A arg) {
        for (var handler : this.handlers.get(event)) {
            ((Consumer<A>) handler).accept(arg);
        }
    }

    public <A> void subscribe(Event<A> event, Consumer<A> handler) {
        this.handlers.put(event, handler);
    }

    public void subscribe(Event<Unit> event, Runnable handler) {
        this.handlers.put(event, $ -> handler.run());
    }

    public static <A> void invoke(BlockEntity be, Event<A> event, A arg) {
        be.getCapability(AllCapabilities.EVENT_MANAGER.get())
                .ifPresent(eventManager -> eventManager.invoke(event, arg));
    }

    public static void invoke(BlockEntity be, Event<Unit> event) {
        invoke(be, event, Unit.INSTANCE);
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
