package org.shsts.tinactory.api.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.recipe.IProcessingObject;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMachineProcessor extends IProcessor {
    Optional<IProcessingObject> getInfo(int port, int index);
}
