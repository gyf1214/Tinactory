package org.shsts.tinactory.api.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import org.shsts.tinactory.api.gui.IRenderDescriptor;

import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IProcessingDisplay {
    IRenderDescriptor display();

    default Optional<List<Component>> tooltip() {
        return Optional.empty();
    }
}
