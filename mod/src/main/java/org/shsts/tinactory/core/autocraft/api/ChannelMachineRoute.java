package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.core.autocraft.pattern.CraftKey;
import org.shsts.tinactory.core.logistics.CraftPortChannel;
import org.shsts.tinactory.core.logistics.IStackAdapter;

import java.util.function.BiFunction;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class ChannelMachineRoute<T> implements IMachineRoute {
    private final CraftKey key;
    private final Direction direction;
    private final CraftPortChannel<T> channel;

    public ChannelMachineRoute(
        CraftKey key,
        Direction direction,
        IStackAdapter<T> stackAdapter,
        IPort<T> port,
        BiFunction<CraftKey, Integer, T> stackOf,
        Function<T, CraftKey> keyOf) {
        this(key, direction, new CraftPortChannel<>(stackAdapter, port, stackOf, keyOf));
    }

    public ChannelMachineRoute(CraftKey key, Direction direction, CraftPortChannel<T> channel) {
        this.key = key;
        this.direction = direction;
        this.channel = channel;
    }

    @Override
    public CraftKey key() {
        return key;
    }

    @Override
    public Direction direction() {
        return direction;
    }

    @Override
    public long transfer(long amount, boolean simulate) {
        return switch (direction) {
            case INPUT -> channel.insert(key, amount, simulate);
            case OUTPUT -> channel.extract(key, amount, simulate);
        };
    }
}
