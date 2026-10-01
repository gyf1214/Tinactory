package org.shsts.tinactory.content.tool;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Tool;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PoweredDrillItem extends PoweredToolItem {
    private final int areaMiningRadius;

    public PoweredDrillItem(Item.Properties properties, PoweredToolConfig config, int areaMiningRadius) {
        super(properties.component(DataComponents.TOOL, tool(config)), config);
        this.areaMiningRadius = areaMiningRadius;
    }

    public int areaMiningRadius() {
        return areaMiningRadius;
    }

    private static Tool tool(PoweredToolConfig config) {
        return new Tool(List.of(
            Tool.Rule.deniesDrops(config.harvestTier().getIncorrectBlocksForDrops()),
            Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_PICKAXE, config.miningSpeed()),
            Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_SHOVEL, config.miningSpeed())), 1.0F, 0);
    }
}
