package org.shsts.tinactory.api.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

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

    /**
     * If this returns false, extract and
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
