package org.shsts.tinactory.registrate.handler;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.event.ColorHandlerEvent;

import java.util.HashMap;
import java.util.Map;

public class TintHandler {
    private final Map<Block, BlockColor> blockColors = new HashMap<>();
    private final Map<Item, ItemColor> itemColors = new HashMap<>();

    public void addBlockColor(Block block, BlockColor blockColor) {
        this.blockColors.put(block, blockColor);
    }

    public void addItemColor(Item item, ItemColor itemColor) {
        this.itemColors.put(item, itemColor);
    }

    public void onRegisterBlockColors(ColorHandlerEvent.Block event) {
        for (var entry : this.blockColors.entrySet()) {
            event.getBlockColors().register(entry.getValue(), entry.getKey());
        }
    }

    public void onRegisterItemColors(ColorHandlerEvent.Item event) {
        for (var entry : this.itemColors.entrySet()) {
            event.getItemColors().register(entry.getValue(), entry.getKey());
        }
    }
}
