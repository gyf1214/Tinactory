package org.shsts.tinactory.api.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IPort {
    PortType type();

    default IItemCollection asItem() {
        assert type() == PortType.ITEM;
        return (IItemCollection) this;
    }

    default IFluidCollection asFluid() {
        assert type() == PortType.FLUID;
        return (IFluidCollection) this;
    }

    boolean isEmpty();

    IPort EMPTY = new IPort() {
        @Override
        public PortType type() {
            return PortType.NONE;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    };
}
