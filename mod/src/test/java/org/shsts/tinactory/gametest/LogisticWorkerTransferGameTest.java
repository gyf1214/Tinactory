package org.shsts.tinactory.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.items.IItemHandler;
import org.shsts.tinactory.AllBlockEntities;
import org.shsts.tinactory.AllItems;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.content.logistics.LogisticWorkerConfig;
import org.shsts.tinactory.content.tool.BatteryItem;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.integration.network.CableBlock;
import org.shsts.tinactory.integration.network.MachineBlock;

import java.util.Objects;

import static org.shsts.tinactory.AllCapabilities.FLUID_HANDLER;
import static org.shsts.tinactory.AllCapabilities.ITEM_HANDLER;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllCapabilities.MENU_ITEM_HANDLER;
import static org.shsts.tinactory.content.logistics.LogisticWorkerConfig.PREFIX;

@GameTestHolder(TinactoryKeys.ID)
public final class LogisticWorkerTransferGameTest {
    private static final Voltage VOLTAGE = Voltage.MV;
    private static final BlockPos SOURCE = new BlockPos(1, 2, 1);
    private static final BlockPos CENTRAL_CABLE = new BlockPos(2, 2, 1);
    private static final BlockPos DESTINATION = new BlockPos(3, 2, 1);
    private static final BlockPos BATTERY = new BlockPos(2, 1, 1);
    private static final BlockPos WORKER = new BlockPos(2, 3, 1);

    @GameTest(timeoutTicks = 70)
    public static void testItemTransferUsesCombinedWorkerBandwidth(GameTestHelper helper) {
        var route = placeRoute(helper, "logistics/electric_chest", "logistics/electric_chest");
        var source = ITEM_HANDLER.get(helper.getBlockEntity(route.source()));
        require(helper, source.insertItem(0, stack(Items.IRON_INGOT, 8), false).isEmpty(),
            "Could not fill the item source with iron", route.source());
        require(helper, source.insertItem(1, stack(Items.GOLD_INGOT, 8), false).isEmpty(),
            "Could not fill the item source with gold", route.source());
        configureWorker(helper, route, null);
        useWithMockPlayer(helper, route.source());

        helper.runAfterDelay(45, () -> {
            var destination = ITEM_HANDLER.get(helper.getBlockEntity(route.destination()));
            require(helper, itemAmount(destination, Items.IRON_INGOT) == 8,
                "Worker did not move all iron in one bandwidth operation", route.destination());
            require(helper, itemAmount(destination, Items.GOLD_INGOT) == 8,
                "Worker did not move all gold in one bandwidth operation", route.destination());
            helper.succeed();
        });
    }

    @GameTest(timeoutTicks = 70)
    public static void testUnfilteredFluidTransferUsesCombinedWorkerBandwidth(GameTestHelper helper) {
        var route = placeRoute(helper, "logistics/electric_tank", "logistics/electric_tank");
        var source = FLUID_HANDLER.get(helper.getBlockEntity(route.source()));
        require(helper, source.fill(new FluidStack(Fluids.WATER, 2000), IFluidHandler.FluidAction.EXECUTE) == 2000,
            "Could not fill the fluid source with water", route.source());
        require(helper, source.fill(new FluidStack(Fluids.LAVA, 2000), IFluidHandler.FluidAction.EXECUTE) == 2000,
            "Could not fill the fluid source with lava", route.source());
        configureWorker(helper, route, null);
        useWithMockPlayer(helper, route.source());

        helper.runAfterDelay(45, () -> {
            var destination = FLUID_HANDLER.get(helper.getBlockEntity(route.destination()));
            require(helper, fluidAmount(destination, Fluids.WATER) == 2000,
                "Worker did not move all water in one bandwidth operation", route.destination());
            require(helper, fluidAmount(destination, Fluids.LAVA) == 2000,
                "Worker did not move all lava in one bandwidth operation", route.destination());
            helper.succeed();
        });
    }

    @GameTest(timeoutTicks = 70)
    public static void testFilteredFluidTransferUsesIdentityAndWorkerBandwidth(GameTestHelper helper) {
        var route = placeRoute(helper, "logistics/electric_tank", "logistics/electric_tank");
        var source = FLUID_HANDLER.get(helper.getBlockEntity(route.source()));
        require(helper, source.fill(new FluidStack(Fluids.WATER, 10000), IFluidHandler.FluidAction.EXECUTE) == 10000,
            "Could not fill the fluid source with water", route.source());
        require(helper, source.fill(new FluidStack(Fluids.LAVA, 2000), IFluidHandler.FluidAction.EXECUTE) == 2000,
            "Could not fill the fluid source with lava", route.source());
        configureWorker(helper, route, new FluidStack(Fluids.WATER, 1));
        useWithMockPlayer(helper, route.source());

        helper.runAfterDelay(45, () -> {
            var destination = FLUID_HANDLER.get(helper.getBlockEntity(route.destination()));
            var remaining = FLUID_HANDLER.get(helper.getBlockEntity(route.source()));
            require(helper, fluidAmount(destination, Fluids.WATER) == 8000,
                "Filtered fluid transfer was capped by the filter marker amount", route.destination());
            require(helper, fluidAmount(destination, Fluids.LAVA) == 0,
                "Filtered fluid transfer moved a non-selected fluid", route.destination());
            require(helper, fluidAmount(remaining, Fluids.WATER) == 2000,
                "Filtered fluid transfer ignored the worker bandwidth", route.source());
            require(helper, fluidAmount(remaining, Fluids.LAVA) == 2000,
                "Filtered fluid transfer changed a non-selected source fluid", route.source());
            helper.succeed();
        });
    }

    private static Route placeRoute(GameTestHelper helper, String sourceName, String destinationName) {
        helper.setBlock(SOURCE, machineState(sourceName, Direction.EAST));
        helper.setBlock(CENTRAL_CABLE, cableState());
        helper.setBlock(DESTINATION, machineState(destinationName, Direction.WEST));
        helper.setBlock(BATTERY, machineState("battery_box", Direction.UP));
        helper.setBlock(WORKER, machineState("logistics/logistic_worker", Direction.DOWN));

        var battery = batteryItem();
        var batteryStack = new ItemStack(battery);
        battery.setPower(batteryStack, battery.capacity);
        require(helper, MENU_ITEM_HANDLER.get(helper.getBlockEntity(BATTERY))
                .insertItem(0, batteryStack, false).isEmpty(),
            "Could not charge the logistics test battery", BATTERY);
        return new Route(SOURCE, WORKER, DESTINATION);
    }

    private static void configureWorker(GameTestHelper helper, Route route, FluidStack fluidFilter) {
        var source = MACHINE.get(helper.getBlockEntity(route.source()));
        var worker = MACHINE.get(helper.getBlockEntity(route.worker()));
        var destination = MACHINE.get(helper.getBlockEntity(route.destination()));
        var config = new LogisticWorkerConfig();
        config.setValid(true);
        config.setFrom(source.uuid(), 0);
        config.setTo(destination.uuid(), 0);
        if (fluidFilter == null) {
            config.clearFilter();
        } else {
            config.setFilter(fluidFilter);
        }
        worker.setConfig(SetMachineConfigPacket.builder()
            .set(PREFIX + 0, config.serializeNBT(helper.getLevel().registryAccess())).get());
    }

    private static int itemAmount(IItemHandler handler, Item item) {
        var amount = 0;
        for (var slot = 0; slot < handler.getSlots(); slot++) {
            var stack = handler.getStackInSlot(slot);
            if (stack.is(item)) {
                amount += stack.getCount();
            }
        }
        return amount;
    }

    private static int fluidAmount(IFluidHandler handler, Fluid fluid) {
        var amount = 0;
        for (var tank = 0; tank < handler.getTanks(); tank++) {
            var stack = handler.getFluidInTank(tank);
            if (stack.getFluid() == fluid) {
                amount += stack.getAmount();
            }
        }
        return amount;
    }

    private static ItemStack stack(Item item, int count) {
        var ret = new ItemStack(item);
        ret.setCount(count);
        return ret;
    }

    private static BlockState machineState(String name, Direction ioFacing) {
        return Objects.requireNonNull(AllBlockEntities.getMachine(name), name)
            .block(VOLTAGE)
            .defaultBlockState()
            .setValue(MachineBlock.IO_FACING, ioFacing);
    }

    private static BlockState cableState() {
        return componentBlock("cable").defaultBlockState()
            .setValue(CableBlock.EAST, true)
            .setValue(CableBlock.WEST, true)
            .setValue(CableBlock.UP, true)
            .setValue(CableBlock.DOWN, true);
    }

    private static Block componentBlock(String name) {
        return (Block) AllItems.getComponent(name).get(VOLTAGE).get();
    }

    private static BatteryItem batteryItem() {
        return (BatteryItem) AllItems.getComponent("battery").get(VOLTAGE).get();
    }

    private static void useWithMockPlayer(GameTestHelper helper, BlockPos pos) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var absolutePos = helper.absolutePos(pos);
        var state = helper.getLevel().getBlockState(absolutePos);
        state.useItemOn(ItemStack.EMPTY, helper.getLevel(), player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(absolutePos), Direction.NORTH, absolutePos, true));
    }

    private static void require(GameTestHelper helper, boolean condition, String message, BlockPos pos) {
        if (!condition) {
            helper.fail(message, pos);
        }
    }

    private record Route(BlockPos source, BlockPos worker, BlockPos destination) {}
}
