package org.shsts.tinactory.api.recipe;

import org.shsts.tinactory.api.logistics.IPort;

import java.util.Optional;
import java.util.Random;

public interface IProcessingResult extends IProcessingObject {
    Optional<IProcessingResult> insertPort(IPort<?> port, int parallel, Random random, boolean simulate);
}
