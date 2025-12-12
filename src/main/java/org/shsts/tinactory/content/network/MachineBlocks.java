package org.shsts.tinactory.content.network;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import org.shsts.tinactory.core.common.SmartEntityBlock;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.multiblock.MultiblockInterfaceBlock;
import org.shsts.tinactory.core.network.MachineBlock;
import org.shsts.tinactory.core.network.SidedMachineBlock;

import java.util.List;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MachineBlocks {
    public static SmartEntityBlock.Factory<MachineBlock> processing(Voltage voltage) {
        return (properties, entityType, menu) ->
            new MachineBlock(properties, entityType, menu, voltage);
    }

    public static SmartEntityBlock.Factory<MachineBlock> simple(Consumer<List<Component>> tooltipBuilder) {
        return (properties, entityType, menu) -> new StaticMachineBlock(properties, entityType, menu) {
            @Override
            public void appendHoverText(ItemStack stack, @Nullable BlockGetter world,
                List<Component> tooltip, TooltipFlag isAdvanced) {
                super.appendHoverText(stack, world, tooltip, isAdvanced);
                tooltipBuilder.accept(tooltip);
            }
        };
    }

    public static SmartEntityBlock.Factory<MachineBlock> multiblockInterface(Voltage voltage,
        Consumer<List<Component>> tooltipBuilder) {
        return (properties, entityType, menu) -> new MultiblockInterfaceBlock(
            properties, entityType, menu, voltage) {
            @Override
            public void appendHoverText(ItemStack stack, @Nullable BlockGetter world,
                List<Component> tooltip, TooltipFlag isAdvanced) {
                super.appendHoverText(stack, world, tooltip, isAdvanced);
                tooltipBuilder.accept(tooltip);
            }
        };
    }

    public static SmartEntityBlock.Factory<MachineBlock> batteryBox(Voltage voltage,
        Consumer<List<Component>> tooltipBuilder) {
        return (properties, entityType, menu) -> new SidedMachineBlock(properties, entityType, menu, voltage) {
            @Override
            public void appendHoverText(ItemStack stack, @Nullable BlockGetter world,
                List<Component> tooltip, TooltipFlag isAdvanced) {
                super.appendHoverText(stack, world, tooltip, isAdvanced);
                tooltipBuilder.accept(tooltip);
            }
        };
    }

    public static SmartEntityBlock.Factory<MachineBlock> signal(double power) {
        return (properties, entityType, menu) -> new SignalMachineBlock(properties, entityType, menu, power);
    }
}
