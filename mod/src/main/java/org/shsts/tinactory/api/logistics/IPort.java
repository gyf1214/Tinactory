package org.shsts.tinactory.api.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IPort {
    PortType type();

    default IItemPort asItem() {
        assert type() == PortType.ITEM;
        return (IItemPort) this;
    }

    @SuppressWarnings("unchecked")
    default <T> IPortFilter<T> asFilter() {
        assert type() != PortType.NONE;
        return (IPortFilter<T>) this;
    }

    default IFluidPort asFluid() {
        assert type() == PortType.FLUID;
        return (IFluidPort) this;
    }

    /**
     * If this returns false, extract and getItem should return empty.
     */
    boolean acceptOutput();

    IPort EMPTY = new IPort() {
        @Override
        public PortType type() {
            return PortType.NONE;
        }

        @Override
        public boolean acceptOutput() {
            return false;
        }
    };
}
