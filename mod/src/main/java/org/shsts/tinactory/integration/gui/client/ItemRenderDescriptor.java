package org.shsts.tinactory.integration.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.core.gui.IRenderDescriptor;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ItemRenderDescriptor(ItemStack stack) implements IRenderDescriptor {}
