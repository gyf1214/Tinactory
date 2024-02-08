package org.shsts.tinactory.content.recipe;

import org.shsts.tinactory.content.logistics.IPort;

import java.util.Random;

public interface IProcessingResult {
    boolean insertPort(IPort port, Random random, boolean simulate);
}
