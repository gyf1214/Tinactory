package org.shsts.tinactory;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.content.gui.sync.OpenTechPacket;
import org.shsts.tinactory.content.tool.WrenchOutlineRenderer;
import org.shsts.tinactory.core.tool.IWrenchable;
import org.shsts.tinactory.core.tool.UsableToolItem;

import static org.shsts.tinactory.Tinactory.CHANNEL;

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

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (OPEN_TECH.consumeClick()) {
                CHANNEL.sendToServer(OpenTechPacket.INSTANCE);
            }
        }
    }

    public static KeyMapping OPEN_TECH;

    public static void initKeys() {
        OPEN_TECH = createKey("open_tech", InputConstants.KEY_GRAVE, KeyMapping.CATEGORY_INTERFACE);
    }

    private static KeyMapping createKey(String id, int keycode, String category) {
        var key = new KeyMapping("key." + TinactoryKeys.ID + "." + id, keycode, category);
        ClientRegistry.registerKeyBinding(key);
        return key;
    }
}
