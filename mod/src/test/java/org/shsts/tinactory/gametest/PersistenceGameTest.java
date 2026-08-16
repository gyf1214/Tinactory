package org.shsts.tinactory.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.content.logistics.ElectricChest;
import org.shsts.tinactory.content.logistics.ElectricTank;
import org.shsts.tinactory.content.machine.IBoiler;

import static org.shsts.tinactory.AllCapabilities.PROCESSOR;
import static org.shsts.tinactory.integration.common.CapabilityProvider.getContainer;

@GameTestHolder(TinactoryKeys.ID)
public final class PersistenceGameTest {
    @GameTest
    public static void testIdleBoilerHasNoProcessingInfo(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("machine/boiler/low"));
        var boiler = (IBoiler) PROCESSOR.get(helper.getBlockEntity(pos));
        if (!boiler.getAllInfo().isEmpty()) {
            helper.fail("Idle boiler reported processing info", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testElectricChestSavesAndLoadsItemFilter(GameTestHelper helper) {
        var sourcePos = new BlockPos(1, 1, 1);
        var destinationPos = new BlockPos(3, 1, 1);
        var block = block("logistics/ulv/electric_chest");
        helper.setBlock(sourcePos, block);
        var chest = getContainer(helper.getBlockEntity(sourcePos), ElectricChest.ID, ElectricChest.class);
        chest.setFilter(0, new ItemStack(Items.DIAMOND));
        var tag = helper.getBlockEntity(sourcePos).saveWithFullMetadata(helper.getLevel().registryAccess());

        helper.setBlock(destinationPos, block);
        helper.getBlockEntity(destinationPos).loadWithComponents(tag, helper.getLevel().registryAccess());
        var restored = getContainer(helper.getBlockEntity(destinationPos), ElectricChest.ID, ElectricChest.class);
        if (restored.getFilter(0).filter(stack -> stack.is(Items.DIAMOND)).isEmpty()) {
            helper.fail("Electric Chest did not restore its item filter", destinationPos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testElectricTankSavesAndLoadsFluidFilter(GameTestHelper helper) {
        var sourcePos = new BlockPos(1, 1, 1);
        var destinationPos = new BlockPos(3, 1, 1);
        var block = block("logistics/ulv/electric_tank");
        helper.setBlock(sourcePos, block);
        var tank = getContainer(helper.getBlockEntity(sourcePos), ElectricTank.ID, ElectricTank.class);
        tank.setFilter(0, new FluidStack(Fluids.WATER, 1000));
        var tag = helper.getBlockEntity(sourcePos).saveWithFullMetadata(helper.getLevel().registryAccess());

        helper.setBlock(destinationPos, block);
        helper.getBlockEntity(destinationPos).loadWithComponents(tag, helper.getLevel().registryAccess());
        var restored = getContainer(helper.getBlockEntity(destinationPos), ElectricTank.ID, ElectricTank.class);
        var filter = restored.getFilter(0);
        if (!filter.is(Fluids.WATER) || filter.getAmount() != 1000) {
            helper.fail("Electric Tank did not restore its fluid filter", destinationPos);
            return;
        }
        helper.succeed();
    }

    private static Block block(String path) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("tinactory", path));
    }
}
