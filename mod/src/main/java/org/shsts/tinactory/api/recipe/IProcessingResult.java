package org.shsts.tinactory.api.recipe;

import net.minecraft.util.RandomSource;
import org.shsts.tinactory.api.logistics.IPort;

import java.util.Optional;

public interface IProcessingResult extends IProcessingObject {
    Optional<IProcessingResult> insertPort(IPort<?> port, int parallel, RandomSource random, boolean simulate);

    IProcessingResult scaledPreview(int parallel);
}
