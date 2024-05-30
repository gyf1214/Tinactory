package org.shsts.tinactory.api.recipe;

import org.shsts.tinactory.api.logistics.IPort;

import java.util.Random;

public interface IProcessingResult extends IProcessingObject {
    boolean insertPort(IPort port, Random random, boolean simulate);
}
