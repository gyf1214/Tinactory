package org.shsts.tinactory.registrate.handler;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.HashMap;
import java.util.Map;

public class RenderTypeHandler {
    private final Map<Block, RenderType> renderTypes = new HashMap<>();

    public void setRenderType(Block block, RenderType renderType) {
        renderTypes.put(block, renderType);
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        for (var entry : renderTypes.entrySet()) {
            ItemBlockRenderTypes.setRenderLayer(entry.getKey(), entry.getValue());
        }
    }
}
