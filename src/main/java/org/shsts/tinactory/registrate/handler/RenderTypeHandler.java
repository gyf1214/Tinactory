package org.shsts.tinactory.registrate.handler;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
public class RenderTypeHandler {
    private final Map<Block, RenderType> renderTypes = new HashMap<>();

    public void setRenderType(Block block, RenderType renderType) {
        renderTypes.put(block, renderType);
    }

    public void onClientSetup() {
        for (var entry : renderTypes.entrySet()) {
            ItemBlockRenderTypes.setRenderLayer(entry.getKey(), entry.getValue());
        }
    }
}
