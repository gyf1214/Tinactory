package org.shsts.tinactory.api.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import org.shsts.tinactory.api.gui.IRenderDescriptor;

import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IStackKey {
    PortType type();

    IStackAdapter<?> adapter();

    default IRenderDescriptor display() {
        return adapter().display(this);
    }

    default Component name() {
        return adapter().name(this);
    }

    default Optional<List<Component>> tooltip() {
        return adapter().tooltip(this);
    }
}
