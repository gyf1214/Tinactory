package org.shsts.tinactory.core.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.recipe.IProcessingObject;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IProcessingAdapter {
    Optional<IMachine> machine();

    boolean isClientSide();

    void onUpdate();

    void reportObject(PortDirection direction, IProcessingObject object);
}
