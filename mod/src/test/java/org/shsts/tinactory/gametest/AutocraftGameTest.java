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
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.api.logistics.ContainerAccess;
import org.shsts.tinactory.api.logistics.IItemPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.content.autocraft.MECraftCpu;
import org.shsts.tinactory.content.autocraft.MECraftTerminal;
import org.shsts.tinactory.content.tool.BatteryItem;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.TargetRecipeConstraint;
import org.shsts.tinactory.core.autocraft.plan.PlanError;
import org.shsts.tinactory.core.autocraft.plan.PlanResult;
import org.shsts.tinactory.core.autocraft.service.AutocraftTerminalService;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.integration.common.CapabilityProvider;
import org.shsts.tinactory.integration.machine.Machine;
import org.shsts.tinactory.integration.network.CableBlock;
import org.shsts.tinactory.integration.network.MachineBlock;

import java.util.List;
import java.util.UUID;

import static org.shsts.tinactory.AllCapabilities.ITEM_PORT_ITEM;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllCapabilities.MENU_ITEM_HANDLER;
import static org.shsts.tinactory.AllCapabilities.PATTERN_CELL_ITEM;
import static org.shsts.tinactory.AllNetworks.BATTERY_DISCHARGE;
import static org.shsts.tinactory.AllNetworks.ELECTRIC_COMPONENT;
import static org.shsts.tinactory.integration.common.CapabilityProvider.getContainer;
import static org.shsts.tinactory.integration.logistics.StackHelper.ITEM_ADAPTER;

@GameTestHolder(TinactoryKeys.ID)
public final class AutocraftGameTest {
    private static final Voltage VOLTAGE = Voltage.HV;
    private static final BlockPos CABLE = new BlockPos(2, 2, 2);
    private static final BlockPos DRIVE = CABLE.west();
    private static final BlockPos CRAFT_TERMINAL = CABLE.east();
    private static final BlockPos CPU = CABLE.north();
    private static final BlockPos PROCESSOR = CABLE.south();
    private static final BlockPos BATTERY_CABLE = CABLE.below();
    private static final BlockPos BATTERY = BATTERY_CABLE.east();
    private static final BlockPos BATTERY_2 = BATTERY_CABLE.south();
    private static final BlockPos PATTERN_TERMINAL = CABLE.above();

    private static final Item INPUT_ITEM = Items.AMETHYST_SHARD;
    private static final Item OUTPUT_ITEM = Items.DIAMOND;
    private static final UUID PATTERN_ID = UUID.fromString("b5ca4f4e-7b33-4b48-9dcf-b1f9e17d7ea4");

    @GameTest(timeoutTicks = 60)
    public static void testAutocraftPlanningUsesLiveNetwork(GameTestHelper helper) {
        placeNetwork(helper, true, false);
        helper.runAfterDelay(12, () -> {
            var service = craftService(helper);
            if (service == null) {
                return;
            }
            var input = new ItemStack(INPUT_ITEM);
            var output = new ItemStack(OUTPUT_ITEM);
            var result = service.preview(ITEM_ADAPTER.keyOf(output), 1);

            if (result.plan() == null || result.plan().steps().size() != 1) {
                helper.fail("Live autocraft planning did not produce one executable step", CRAFT_TERMINAL);
                return;
            }
            if (service.listRequestables().stream().noneMatch(ITEM_ADAPTER.keyOf(output)::equals)) {
                helper.fail("Live pattern repository did not expose the stored output", PATTERN_TERMINAL);
                return;
            }
            if (itemPort(helper).getStorageAmount(input) != 1) {
                helper.fail("Planning consumed the source input", DRIVE);
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(timeoutTicks = 60)
    public static void testAutocraftPlanningReportsMissingAndInsufficientInputs(GameTestHelper helper) {
        placeNetwork(helper, false, false);
        helper.runAfterDelay(12, () -> {
            var service = craftService(helper);
            if (service == null) {
                return;
            }
            var knownOutput = ITEM_ADAPTER.keyOf(new ItemStack(OUTPUT_ITEM));
            var missing = service.preview(ITEM_ADAPTER.keyOf(new ItemStack(Items.NETHER_STAR)), 1);
            var zero = service.preview(knownOutput, 0);
            var insufficient = service.preview(knownOutput, 1);

            if (!hasError(missing, PlanError.Code.MISSING_PATTERN) ||
                !hasError(zero, PlanError.Code.MISSING_PATTERN) ||
                !hasError(insufficient, PlanError.Code.UNSATISFIED_BASE_RESOURCE)) {
                helper.fail("Live autocraft planning did not report expected negative cases", CRAFT_TERMINAL);
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(timeoutTicks = 180)
    public static void testAutocraftExecutesFastMachineRecipe(GameTestHelper helper) {
        placeNetwork(helper, true, true);
        helper.runAfterDelay(12, () -> {
            var service = craftService(helper);
            if (service == null) {
                return;
            }
            var output = new ItemStack(OUTPUT_ITEM);
            var result = service.preview(ITEM_ADAPTER.keyOf(output), 1);
            if (result.plan() == null) {
                helper.fail("Fast autocraft recipe could not be planned", CRAFT_TERMINAL);
                return;
            }
            var cpuId = getContainer(helper.getBlockEntity(CPU), MECraftCpu.ID, MECraftCpu.class)
                .status().cpuId();
            if (!service.execute(cpuId)) {
                helper.fail("Autocraft terminal rejected the prepared fast recipe", CRAFT_TERMINAL);
                return;
            }
            helper.runAfterDelay(120, () -> {
                var outputAmount = itemPort(helper).getStorageAmount(output);
                var inputAmount = itemPort(helper).getStorageAmount(new ItemStack(INPUT_ITEM));
                var cpu = getContainer(helper.getBlockEntity(CPU), MECraftCpu.ID, MECraftCpu.class);
                var status = cpu.status();
                var machine = MACHINE.get(helper.getBlockEntity(PROCESSOR));
                var machineInput = machine.container().orElseThrow()
                    .getPort(0, ContainerAccess.EXTERNAL).asItem()
                    .getStorageAmount(new ItemStack(INPUT_ITEM));
                var machineOutput = 0L;
                var container = machine.container().orElseThrow();
                for (var port = 0; port < container.portSize(); port++) {
                    if (!container.hasPort(port)) {
                        continue;
                    }
                    var external = container.getPort(port, ContainerAccess.EXTERNAL);
                    if (external.type() == PortType.ITEM) {
                        machineOutput += container.getPort(port, ContainerAccess.EXTERNAL).asItem()
                            .getStorageAmount(output);
                    }
                }
                var workFactor = machine.network().map(network ->
                    network.getComponent(ELECTRIC_COMPONENT.get()).getWorkFactor()).orElse(-1d);
                var progress = machine.processor().map(processor -> processor.getProgress()).orElse(-1d);
                if (outputAmount != 1 || inputAmount != 0) {
                    helper.fail("Fast autocraft recipe did not finish: output=" + outputAmount +
                        ", input=" + inputAmount + ", state=" + status.state() +
                        ", error=" + status.error() + ", completed=" + status.completedSteps() +
                        "/" + status.totalSteps() + ", machineInput=" + machineInput +
                        ", machineOutput=" + machineOutput + ", workFactor=" + workFactor +
                        ", progress=" + progress,
                        PROCESSOR);
                    return;
                }
                helper.succeed();
            });
        });
    }

    private static boolean hasError(PlanResult result, PlanError.Code code) {
        return result.plan() == null && result.error() != null && result.error().code() == code;
    }

    private static AutocraftTerminalService craftService(GameTestHelper helper) {
        var terminal = getContainer(helper.getBlockEntity(CRAFT_TERMINAL), MECraftTerminal.ID,
            MECraftTerminal.class);
        var service = terminal.createService();
        if (service == null) {
            helper.fail("Craft terminal did not connect to the autocraft network", CRAFT_TERMINAL);
        }
        return service;
    }

    private static void placeNetwork(GameTestHelper helper, boolean withInput, boolean withProcessor) {
        helper.setBlock(DRIVE, machineState("logistics/me_drive", Direction.EAST));
        helper.setBlock(CRAFT_TERMINAL, machineState("logistics/me_craft_terminal", Direction.WEST));
        helper.setBlock(CPU, machineState("logistics/me_craft_cpu/basic", Direction.SOUTH));
        helper.setBlock(PATTERN_TERMINAL, machineState("logistics/me_pattern_terminal", Direction.DOWN));
        helper.setBlock(BATTERY_CABLE, cableState());
        helper.setBlock(BATTERY, machineState("battery_box", Direction.WEST));
        helper.setBlock(BATTERY_2, machineState("battery_box", Direction.NORTH));
        if (withProcessor) {
            helper.setBlock(PROCESSOR, machineState("ore_analyzer", Direction.NORTH));
        }
        helper.setBlock(CABLE, cableState());

        var battery = (BatteryItem) AllItems.getComponent("battery").get(VOLTAGE).get();
        for (var batteryPos : List.of(BATTERY, BATTERY_2)) {
            var batteryStack = new ItemStack(battery);
            battery.setPower(batteryStack, battery.capacity);
            var batteryEntity = helper.getBlockEntity(batteryPos);
            var batteryMachine = CapabilityProvider.getContainer(batteryEntity, "network/machine", Machine.class);
            batteryMachine.config().apply(SetMachineConfigPacket.builder()
                .set(BATTERY_DISCHARGE, true).get());
            MENU_ITEM_HANDLER.get(batteryEntity).insertItem(0, batteryStack, false);
        }

        var itemCell = new ItemStack(item("logistics/item_storage_cell/tier_1"));
        var patternCell = new ItemStack(item("logistics/pattern_cell/tier_1"));
        var input = new ItemStack(INPUT_ITEM);
        var inputPort = ITEM_PORT_ITEM.tryGet(itemCell).orElseThrow();
        if (withInput && !inputPort.insert(input, false).isEmpty()) {
            helper.fail("Could not seed the ME storage cell with autocraft input", DRIVE);
        }
        var pattern = new CraftPattern(
            PATTERN_ID,
            List.of(new CraftAmount(ITEM_ADAPTER.keyOf(input), 1)),
            List.of(new CraftAmount(ITEM_ADAPTER.keyOf(new ItemStack(OUTPUT_ITEM)), 1)),
            List.of(new TargetRecipeConstraint(
                ResourceLocation.fromNamespaceAndPath(TinactoryKeys.ID, "gametest/ore_analyzer/autocraft"))));
        if (!PATTERN_CELL_ITEM.tryGet(patternCell).orElseThrow().insert(pattern)) {
            helper.fail("Could not store the autocraft test pattern", DRIVE);
        }
        var driveHandler = MENU_ITEM_HANDLER.get(helper.getBlockEntity(DRIVE));
        if (!driveHandler.insertItem(0, itemCell, false).isEmpty() ||
            !driveHandler.insertItem(1, patternCell, false).isEmpty()) {
            helper.fail("ME drive rejected the test storage cells", DRIVE);
        }

        useWithMockPlayer(helper, DRIVE);
    }

    private static Item item(String name) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(TinactoryKeys.ID, name));
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

    private static IItemPort itemPort(GameTestHelper helper) {
        return ITEM_PORT_ITEM.tryGet(MENU_ITEM_HANDLER.get(helper.getBlockEntity(DRIVE)).getStackInSlot(0))
            .orElseThrow();
    }
}
