package org.shsts.tinactory.core.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.gui.IRenderDescriptor;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ItemIdRenderDescriptor(ResourceLocation itemId) implements IRenderDescriptor {}
