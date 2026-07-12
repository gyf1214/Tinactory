package org.shsts.tinactory;

import com.mojang.blaze3d.platform.InputConstants;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidType;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.content.gui.sync.OpenTechPacket;
import org.shsts.tinactory.content.tool.WrenchOutlineRenderer;
import org.shsts.tinactory.integration.tool.IWrenchable;
import org.shsts.tinactory.integration.tool.UsableToolItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.shsts.tinactory.Tinactory.CORE;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
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
            CORE.sendToServer(AllMenus.OPEN_TECH, OpenTechPacket.INSTANCE);
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

    private static final List<Consumer<RegisterClientExtensionsEvent>> CLIENT_EXTENSIONS = new ArrayList<>();

    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        for (var cb : CLIENT_EXTENSIONS) {
            cb.accept(event);
        }
    }

    private record SimpleFluidExtension(int color, ResourceLocation tex)
        implements IClientFluidTypeExtensions {
        @Override
        public int getTintColor() {
            return color;
        }

        @Override
        public ResourceLocation getStillTexture() {
            return tex;
        }

        @Override
        public ResourceLocation getFlowingTexture() {
            return tex;
        }
    }

    public static void registerFluidTex(Supplier<? extends FluidType> fluidType, int color, ResourceLocation tex) {
        CLIENT_EXTENSIONS.add(event -> event.registerFluidType(
            new SimpleFluidExtension(color, tex), fluidType.get()));
    }
}
