package org.shsts.tinactory.compat.waila;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.fluids.FluidStack;
import org.shsts.tinactory.integration.common.SmartEntityBlock;
import org.shsts.tinactory.integration.logistics.StackHelper;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

import java.util.List;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@WailaPlugin
@SuppressWarnings("UnstableApiUsage")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Waila implements IWailaPlugin {
    public static final ResourceLocation CONTAINER = modLoc("container");
    public static final ResourceLocation PROCESSOR = modLoc("processor");
    public static final ResourceLocation ELECTRIC = modLoc("electric");
    public static final ResourceLocation MULTIBLOCK = modLoc("multiblock");
    public static final ResourceLocation ME_CRAFT_CPU = modLoc("me_craft_cpu");
    public static final ResourceLocation HIDE_EMPTY_TANK = modLoc("hide_empty_tank");
    public static final ResourceLocation BYTES = modLoc("bytes");
    public static final ResourceLocation HEAT = modLoc("heat");
    public static final ResourceLocation POWER = modLoc("power");
    public static final ResourceLocation CLEANNESS = modLoc("cleanness");
    public static final ResourceLocation FUSION = modLoc("fusion");
    public static final ResourceLocation PROGRESS = modLoc("progress");
    public static final ResourceLocation RECIPE = modLoc("recipe");
    public static final ResourceLocation WORK_SPEED = modLoc("work_speed");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(ContainerProvider.INSTANCE, BlockEntity.class);
        registration.registerBlockDataProvider(MultiblockProvider.INSTANCE, BlockEntity.class);
        registration.registerBlockDataProvider(ProcessorProvider.INSTANCE, BlockEntity.class);
        registration.registerBlockDataProvider(ElectricProvider.INSTANCE, BlockEntity.class);
        registration.registerBlockDataProvider(MECraftCpuProvider.INSTANCE, BlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addConfig(HIDE_EMPTY_TANK, true);
        registration.addConfig(BYTES, true);
        registration.addConfig(HEAT, true);
        registration.addConfig(POWER, true);
        registration.addConfig(CLEANNESS, true);
        registration.addConfig(FUSION, true);
        registration.addConfig(PROGRESS, true);
        registration.addConfig(RECIPE, true);
        registration.addConfig(WORK_SPEED, true);

        registration.registerBlockComponent(ContainerProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(MultiblockProvider.INSTANCE, SmartEntityBlock.class);
        registration.registerBlockComponent(ProcessorProvider.INSTANCE, SmartEntityBlock.class);
        registration.registerBlockComponent(ElectricProvider.INSTANCE, SmartEntityBlock.class);
        registration.registerBlockComponent(MECraftCpuProvider.INSTANCE, SmartEntityBlock.class);
    }

    static {
        ToolHandlers.init();
    }

    private static final Vec2 ITEM_SIZE = new Vec2(10f, 10f);
    private static final Vec2 FLUID_SIZE = new Vec2(8f, 8f);
    private static final Vec2 ITEM_OFFSET = new Vec2(0f, -1f);

    public static void addItemIcon(List<IElement> line, IElementHelper helper, ItemStack stack) {
        var stack1 = StackHelper.copyWithCount(stack, 1);
        line.add(helper.smallItem(stack1).size(ITEM_SIZE).translate(ITEM_OFFSET));
    }

    public static void addFluidIcon(List<IElement> line, IElementHelper helper, FluidStack stack) {
        var fluid = JadeFluidObject.of(stack.getFluid(), stack.getAmount(), stack.getComponentsPatch());
        line.add(helper.spacer(1, 0));
        line.add(helper.fluid(fluid).size(FLUID_SIZE));
        line.add(helper.spacer(1, 0));
    }
}
