package org.shsts.tinactory.integration.waila;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.mobius.waila.api.IWailaClientRegistration;
import mcp.mobius.waila.api.IWailaCommonRegistration;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.core.common.SmartEntityBlock;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@WailaPlugin
@SuppressWarnings("UnstableApiUsage")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Waila implements IWailaPlugin {
    public static final ResourceLocation HIDE_EMPTY_TANK = modLoc("hide_empty_tank");
    public static final ResourceLocation ENHANCE_ITEMS = modLoc("enhance_items");
    public static final ResourceLocation BYTES = modLoc("bytes");
    public static final ResourceLocation HEAT = modLoc("heat");
    public static final ResourceLocation POWER = modLoc("power");
    public static final ResourceLocation CLEANNESS = modLoc("cleanness");
    public static final ResourceLocation PROGRESS = modLoc("progress");
    public static final ResourceLocation RECIPE = modLoc("recipe");
    public static final ResourceLocation WORK_SPEED = modLoc("work_speed");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.addConfig(HIDE_EMPTY_TANK, true);
        registration.addConfig(ENHANCE_ITEMS, true);
        registration.addConfig(BYTES, true);
        registration.addConfig(HEAT, true);
        registration.addConfig(POWER, true);
        registration.addConfig(CLEANNESS, true);
        registration.addConfig(PROGRESS, true);
        registration.addConfig(RECIPE, true);
        registration.addConfig(WORK_SPEED, true);

        registration.registerBlockDataProvider(ContainerProvider.INSTANCE, BlockEntity.class);
        registration.registerBlockDataProvider(ProcessorProvider.INSTANCE, BlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerComponentProvider(ContainerProvider.INSTANCE, TooltipPosition.BODY,
            SmartEntityBlock.class);
        registration.registerComponentProvider(ProcessorProvider.INSTANCE, TooltipPosition.BODY,
            SmartEntityBlock.class);
    }
}
