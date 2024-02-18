package org.shsts.tinactory.api.recipe;

import org.shsts.tinactory.api.logistics.IPort;

import java.util.Random;

public interface IProcessingResult {
    boolean insertPort(IPort port, Random random, boolean simulate);
}
