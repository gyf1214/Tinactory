package org.shsts.tinactory.api.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IPort {
    PortType getPortType();

    default IItemCollection asItem() {
        assert this.getPortType() == PortType.ITEM;
        return (IItemCollection) this;
    }

    default IFluidCollection asFluid() {
        assert this.getPortType() == PortType.FLUID;
        return (IFluidCollection) this;
    }

    IPort EMPTY = () -> PortType.NONE;
}
