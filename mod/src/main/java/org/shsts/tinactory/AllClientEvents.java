package org.shsts.tinactory;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.content.gui.sync.OpenTechPacket;
import org.shsts.tinactory.content.tool.WrenchOutlineRenderer;
import org.shsts.tinactory.integration.tool.IWrenchable;
import org.shsts.tinactory.integration.tool.UsableToolItem;

import static org.shsts.tinactory.AllMenus.OPEN_TECH;
import static org.shsts.tinactory.Tinactory.CORE;

@OnlyIn(Dist.CLIENT)
public final class AllClientEvents {
    @SubscribeEvent
    public static void onDrawHighlight(RenderHighlightEvent.Block event) {
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
    public static void onClientTick(ClientTickEvent.Post event) {
        if (OPEN_TECH.consumeClick()) {
            CORE.sendToServer(OPEN_TECH, OpenTechPacket.INSTANCE);
        }
    }

    public static KeyMapping OPEN_TECH;

    public static void initKeys(RegisterKeyMappingsEvent event) {
        OPEN_TECH = createKey(event, "open_tech", InputConstants.KEY_GRAVE, KeyMapping.CATEGORY_INTERFACE);
    }

    private static KeyMapping createKey(RegisterKeyMappingsEvent event, String id, int keycode, String category) {
        var key = new KeyMapping("key." + TinactoryKeys.ID + "." + id, keycode, category);
        event.register(key);
        return key;
    }
}
