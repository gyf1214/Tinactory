package org.shsts.tinactory.core.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.gui.IRenderDescriptor;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record TextureRenderDescriptor(Texture texture) implements IRenderDescriptor {}
