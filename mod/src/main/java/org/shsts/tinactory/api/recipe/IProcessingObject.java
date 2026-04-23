package org.shsts.tinactory.api.recipe;

import org.shsts.tinactory.api.logistics.PortType;

import java.util.function.Predicate;

public interface IProcessingObject {
    String codecName();

    PortType type();

    Predicate<?> filter();
}
