package org.shsts.tinactory.core.common;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class CapabilityItem extends Item {
    public CapabilityItem(Properties properties) {
        super(properties);
    }

    public abstract void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event);
}
