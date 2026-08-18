package org.shsts.tinactory.api.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import org.shsts.tinactory.api.gui.IRenderDescriptor;
import org.shsts.tinycorelib.api.core.ILoc;

import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IStackKey extends ILoc {
    PortType type();

    IStackAdapter<?> adapter();

    default IRenderDescriptor display() {
        return adapter().display(this);
    }

    default IRenderDescriptor display(long amount) {
        return adapter().display(this, amount);
    }

    default Component name() {
        return adapter().name(this);
    }

    default Optional<List<Component>> tooltip() {
        return adapter().tooltip(this);
    }

    default Optional<List<Component>> tooltip(long amount) {
        return adapter().tooltip(this, amount);
    }
}
