package org.shsts.tinactory.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.shsts.tinactory.AllBlockEntities;
import org.shsts.tinactory.AllItems;
import org.shsts.tinactory.AllMaterials;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.api.logistics.ContainerAccess;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.tool.BatteryItem;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.integration.network.CableBlock;
import org.shsts.tinactory.integration.network.MachineBlock;

import java.util.List;

import static org.shsts.tinactory.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllCapabilities.MENU_ITEM_HANDLER;
import static org.shsts.tinactory.AllNetworks.BATTERY_DISCHARGE;
import static org.shsts.tinactory.AllNetworks.ELECTRIC_COMPONENT;
import static org.shsts.tinactory.AllNetworks.TARGET_RECIPE;

@GameTestHolder(TinactoryKeys.ID)
public final class MachineProcessingGameTest {
    private static final Voltage VOLTAGE = Voltage.HV;
    private static final BlockPos MACHINE_POS = new BlockPos(2, 2, 2);
    private static final BlockPos CABLE_POS = MACHINE_POS.east();
    private static final BlockPos BATTERY_POS = CABLE_POS.east();
    private static final BlockPos BATTERY_2_POS = CABLE_POS.above();
    private static final ResourceLocation AUTOCRAFT_RECIPE = ResourceLocation.fromNamespaceAndPath(
        TinactoryKeys.ID, "gametest/ore_analyzer/autocraft");
    private static final ResourceLocation POWER_PAUSE_RECIPE = ResourceLocation.fromNamespaceAndPath(
        TinactoryKeys.ID, "gametest/ore_analyzer/power_pause");

    @GameTest(timeoutTicks = 80)
    public static void testZeroPowerConsumesInputBeforeProgress(GameTestHelper helper) {
        var machine = placeMachineNetwork(helper, "ore_analyzer", false);
        seedRecipe(machine, AUTOCRAFT_RECIPE, Items.AMETHYST_SHARD);

        helper.runAfterDelay(40, () -> {
            var workFactor = workFactor(machine);
            var input = amount(machine, 0, Items.AMETHYST_SHARD);
            var output = amount(machine, 1, Items.DIAMOND);
            var progress = machine.processor().orElseThrow().getProgress();
            var processorWorking = machine.processor().orElseThrow().isWorking(0d);
            var electric = ELECTRIC_MACHINE.get(machine.blockEntity());
            if (workFactor != 0d || input != 0 || output != 0 || progress != 0d || processorWorking) {
                helper.fail("Zero-power processing state: workFactor=" + workFactor + ", input=" + input +
                    ", output=" + output + ", progress=" + progress + ", electricType=" +
                    electric.getMachineType() + ", power=" + electric.getPowerCons(), MACHINE_POS);
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(timeoutTicks = 180)
    public static void testBlockedOutputKeepsInputUntilSpace(GameTestHelper helper) {
        var machine = placeMachineNetwork(helper, "ore_analyzer", true);
        var output = machine.container().orElseThrow().getPort(1, ContainerAccess.INTERNAL).asItem();
        var remaining = new ItemStack(Items.COBBLESTONE, 64);
        for (var i = 0; i < 16 && !remaining.isEmpty(); i++) {
            remaining = output.insert(remaining, false);
        }
        if (!remaining.isEmpty()) {
            helper.fail("Could not seed the Ore Analyzer output blocker", MACHINE_POS);
            return;
        }
        seedRecipe(machine, AUTOCRAFT_RECIPE, Items.AMETHYST_SHARD);

        helper.runAfterDelay(30, () -> {
            var blocker = amount(machine, 1, Items.COBBLESTONE);
            var progress = machine.processor().orElseThrow().getProgress();
            if (amount(machine, 0, Items.AMETHYST_SHARD) != 1 || progress != 0d) {
                helper.fail("Blocked output did not prevent recipe start: input=" +
                    amount(machine, 0, Items.AMETHYST_SHARD) + ", blocker=" + blocker + ", progress=" + progress +
                    ", workFactor=" + workFactor(machine), MACHINE_POS);
                return;
            }
            output.extract(16 * 64, false);
            helper.runAfterDelay(40, () -> {
                if (amount(machine, 0, Items.AMETHYST_SHARD) != 0 ||
                    amount(machine, 1, Items.DIAMOND) != 1) {
                    helper.fail("Recipe did not complete after output space opened: input=" +
                        amount(machine, 0, Items.AMETHYST_SHARD) + ", output=" +
                        amount(machine, 1, Items.DIAMOND), MACHINE_POS);
                    return;
                }
                helper.succeed();
            });
        });
    }

    @GameTest(timeoutTicks = 240)
    public static void testProcessingPausesAndResumesAcrossPowerLoss(GameTestHelper helper) {
        var machine = placeMachineNetwork(helper, "ore_analyzer", true);
        seedRecipe(machine, POWER_PAUSE_RECIPE, Items.ENDER_PEARL);

        helper.runAfterDelay(20, () -> {
            var progressBeforeLoss = machine.processor().orElseThrow().getProgress();
            if (progressBeforeLoss <= 0d) {
                helper.fail("Power-loss recipe did not start before power removal: progress=" + progressBeforeLoss,
                    MACHINE_POS);
                return;
            }
            removeBatteryPower(helper);
            helper.runAfterDelay(30, () -> {
                var progressWithoutPower = machine.processor().orElseThrow().getProgress();
                if (workFactor(machine) > 1e-4d || progressWithoutPower != progressBeforeLoss ||
                    amount(machine, 1, Items.EMERALD) != 0) {
                    helper.fail("Processing changed without power: workFactor=" + workFactor(machine) +
                        ", before=" + progressBeforeLoss + ", after=" + progressWithoutPower + ", output=" +
                        amount(machine, 1, Items.EMERALD), MACHINE_POS);
                    return;
                }
                restoreBatteryPower(helper);
                helper.runAfterDelay(100, () -> {
                    if (amount(machine, 0, Items.ENDER_PEARL) != 0 ||
                        amount(machine, 1, Items.EMERALD) != 1) {
                        helper.fail("Processing did not resume after power restoration: input=" +
                            amount(machine, 0, Items.ENDER_PEARL) + ", output=" +
                            amount(machine, 1, Items.EMERALD), MACHINE_POS);
                        return;
                    }
                    helper.succeed();
                });
            });
        });
    }

    @GameTest(timeoutTicks = 320)
    public static void testElectricFurnaceProcessesVanillaSmeltingRecipe(GameTestHelper helper) {
        var machine = placeMachineNetwork(helper, "electric_furnace", true);
        var ironDust = AllMaterials.getMaterial("iron").item("dust");
        insertInput(machine, 0, new ItemStack(ironDust));

        helper.runAfterDelay(240, () -> {
            var electric = ELECTRIC_MACHINE.get(machine.blockEntity());
            var input = amount(machine, 0, ironDust);
            var output = amount(machine, 1, Items.IRON_INGOT);
            var progress = machine.processor().orElseThrow().getProgress();
            if (input != 0 || output != 1) {
                helper.fail("Electric furnace did not complete vanilla smelting: input=" +
                    input + ", output=" + output + ", progress=" + progress + ", workFactor=" +
                    workFactor(machine) + ", electricType=" + electric.getMachineType() + ", power=" +
                    electric.getPowerCons(), MACHINE_POS);
                return;
            }
            helper.succeed();
        });
    }

    private static IMachine placeMachineNetwork(GameTestHelper helper, String machineName, boolean withPower) {
        helper.setBlock(MACHINE_POS, machineState(machineName, Direction.EAST));
        helper.setBlock(CABLE_POS, cableState());
        helper.setBlock(BATTERY_POS, machineState("battery_box", Direction.WEST));
        helper.setBlock(BATTERY_2_POS, machineState("battery_box", Direction.DOWN));
        if (withPower) {
            restoreBatteryPower(helper);
        }
        useWithMockPlayer(helper, MACHINE_POS);
        return MACHINE.get(helper.getBlockEntity(MACHINE_POS));
    }

    private static void seedRecipe(IMachine machine, ResourceLocation recipe, Item input) {
        insertInput(machine, 0, new ItemStack(input));
        machine.setConfig(SetMachineConfigPacket.builder().set(TARGET_RECIPE, recipe).get());
    }

    private static void insertInput(IMachine machine, int port, ItemStack input) {
        var remaining = machine.container().orElseThrow().getPort(port, ContainerAccess.EXTERNAL)
            .asItem().insert(input, false);
        if (!remaining.isEmpty()) {
            throw new AssertionError("Machine rejected test input " + input);
        }
    }

    private static long amount(IMachine machine, int port, Item item) {
        return machine.container().orElseThrow().getPort(port, ContainerAccess.INTERNAL)
            .asItem().getStorageAmount(new ItemStack(item));
    }

    private static double workFactor(IMachine machine) {
        return machine.network().orElseThrow().getComponent(ELECTRIC_COMPONENT.get()).getWorkFactor();
    }

    private static void removeBatteryPower(GameTestHelper helper) {
        for (var pos : batteryPositions()) {
            MENU_ITEM_HANDLER.get(helper.getBlockEntity(pos)).extractItem(0, 1, false);
        }
    }

    private static void restoreBatteryPower(GameTestHelper helper) {
        var battery = (BatteryItem) AllItems.getComponent("battery").get(VOLTAGE).get();
        for (var pos : batteryPositions()) {
            var stack = new ItemStack(battery);
            battery.setPower(stack, battery.capacity);
            var batteryMachine = MACHINE.get(helper.getBlockEntity(pos));
            batteryMachine.config().apply(SetMachineConfigPacket.builder()
                .set(BATTERY_DISCHARGE, true).get());
            MENU_ITEM_HANDLER.get(helper.getBlockEntity(pos)).insertItem(0, stack, false);
        }
    }

    private static List<BlockPos> batteryPositions() {
        return List.of(BATTERY_POS, BATTERY_2_POS);
    }

    private static BlockState machineState(String name, Direction ioFacing) {
        var machine = AllBlockEntities.getMachine(name);
        if (machine != null) {
            return machine.block(VOLTAGE).defaultBlockState()
                .setValue(MachineBlock.IO_FACING, ioFacing);
        }
        var block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(TinactoryKeys.ID, name));
        return block.defaultBlockState().setValue(MachineBlock.IO_FACING, ioFacing);
    }

    private static BlockState cableState() {
        return componentBlock("cable").defaultBlockState()
            .setValue(CableBlock.NORTH, true)
            .setValue(CableBlock.EAST, true)
            .setValue(CableBlock.SOUTH, true)
            .setValue(CableBlock.WEST, true)
            .setValue(CableBlock.UP, true)
            .setValue(CableBlock.DOWN, true);
    }

    private static Block componentBlock(String name) {
        return (Block) AllItems.getComponent(name).get(VOLTAGE).get();
    }

    private static void useWithMockPlayer(GameTestHelper helper, BlockPos pos) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var absolutePos = helper.absolutePos(pos);
        var state = helper.getLevel().getBlockState(absolutePos);
        state.useItemOn(ItemStack.EMPTY, helper.getLevel(), player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(absolutePos), Direction.NORTH, absolutePos, true));
    }
}
