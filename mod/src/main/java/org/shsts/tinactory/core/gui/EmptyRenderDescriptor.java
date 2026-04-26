package org.shsts.tinactory.core.gui;

import org.shsts.tinactory.api.gui.IRenderDescriptor;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class EmptyRenderDescriptor implements IRenderDescriptor {
    public static final EmptyRenderDescriptor INSTANCE = new EmptyRenderDescriptor();

    private EmptyRenderDescriptor() {}
}
