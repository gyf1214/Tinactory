package org.shsts.tinactory.registrate;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import org.shsts.tinactory.core.common.Transformer;

import javax.annotation.Nullable;

public interface IItemParent {
    @Nullable
    CreativeModeTab getDefaultCreativeModeTab();

    Transformer<Item.Properties> getDefaultItemProperties();
}
