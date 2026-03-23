package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.core.logistics.CraftPortChannel;
import org.shsts.tinactory.core.logistics.IIngredientKey;
import org.shsts.tinactory.core.logistics.IStackAdapter;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class ChannelMachineRoute<T> implements IMachineRoute {
    private final IIngredientKey key;
    private final PortDirection direction;
    private final CraftPortChannel<T> channel;

    public ChannelMachineRoute(
        IIngredientKey key,
        PortDirection direction,
        IStackAdapter<T> stackAdapter,
        IPort<T> port) {
        this(key, direction, new CraftPortChannel<>(stackAdapter, port));
    }

    public ChannelMachineRoute(IIngredientKey key, PortDirection direction, CraftPortChannel<T> channel) {
        if (direction == PortDirection.NONE) {
            throw new IllegalArgumentException("direction must not be NONE");
        }
        this.key = key;
        this.direction = direction;
        this.channel = channel;
    }

    @Override
    public IIngredientKey key() {
        return key;
    }

    @Override
    public PortDirection direction() {
        return direction;
    }

    @Override
    public long transfer(long amount, boolean simulate) {
        return switch (direction) {
            case INPUT -> channel.insert(key, amount, simulate);
            case OUTPUT -> channel.extract(key, amount, simulate);
            case NONE -> throw new IllegalStateException("route direction must be INPUT or OUTPUT");
        };
    }
}
