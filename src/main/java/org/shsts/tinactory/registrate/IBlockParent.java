package org.shsts.tinactory.registrate;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import org.shsts.tinactory.core.Transformer;

public interface IBlockParent {
    Material getDefaultMaterial();

    Transformer<BlockBehaviour.Properties> getDefaultBlockProperties();
}
