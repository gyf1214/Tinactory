package org.shsts.tinactory.integration.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.core.gui.IRenderDescriptor;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record FluidRenderDescriptor(FluidStack stack) implements IRenderDescriptor {}
