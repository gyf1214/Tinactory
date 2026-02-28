package org.shsts.tinactory.api.recipe;

import org.shsts.tinactory.api.logistics.IPort;

import java.util.Optional;

public interface IProcessingIngredient extends IProcessingObject {
    /**
     * Return the actual ingredient consumed.
     */
    Optional<IProcessingIngredient> consumePort(IPort<?> port, int parallel, boolean simulate);
}
