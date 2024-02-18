package org.shsts.tinactory.content;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.shsts.tinactory.content.tool.IWrenchable;
import org.shsts.tinactory.content.tool.UsableToolItem;
import org.shsts.tinactory.content.tool.WrenchOutlineRenderer;

@OnlyIn(Dist.CLIENT)
public final class AllClientEvents {
    @SubscribeEvent
    public static void onDrawHighlight(DrawSelectionEvent.HighlightBlock event) {
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        var target = event.getTarget();
        var pos = target.getBlockPos();
        var state = mc.level.getBlockState(pos);
        var itemStack = mc.player.getMainHandItem();

        if (state.getBlock() instanceof IWrenchable wrenchable &&
                itemStack.getItem() instanceof UsableToolItem &&
                wrenchable.canWrenchWith(itemStack)) {
            WrenchOutlineRenderer.renderOutlines(event.getPoseStack(), event.getMultiBufferSource(),
                    event.getCamera(), pos, target.getDirection());
        }
    }
}
