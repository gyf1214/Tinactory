package org.shsts.tinactory.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.shsts.tinactory.AllBlockEntities;
import org.shsts.tinactory.AllItems;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.logistics.ContainerAccess;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.multiblock.DigitalInterface;
import org.shsts.tinactory.content.tool.BatteryItem;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.gametest.dependency.DependencyChecker;
import org.shsts.tinactory.integration.network.CableBlock;
import org.shsts.tinactory.integration.network.MachineBlock;
import org.shsts.tinactory.integration.network.WorldNetworkManagers;

import java.util.Objects;

import static org.shsts.tinactory.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllCapabilities.MENU_ITEM_HANDLER;
import static org.shsts.tinactory.AllNetworks.ELECTRIC_COMPONENT;
import static org.shsts.tinactory.AllNetworks.ELECTRIC_SUBNET;
import static org.shsts.tinactory.AllNetworks.LOGISTICS_SUBNET;
import static org.shsts.tinactory.AllNetworks.LOGISTIC_COMPONENT;
import static org.shsts.tinactory.content.electric.BatteryBox.DISCHARGE_KEY;

@GameTestHolder(TinactoryKeys.ID)
public final class TinactoryGameTest {
    private static final String TEAM_NAME = "gametest";

    @BeforeBatch(batch = "defaultBatch")
    public static void setupGameTestTeam(ServerLevel world) {
        var scoreboard = world.getScoreboard();
        if (scoreboard.getPlayerTeam(TEAM_NAME) == null) {
            scoreboard.addPlayerTeam(TEAM_NAME);
        }
    }

    @GameTest
    public static void testSucceed(GameTestHelper helper) {
        helper.succeed();
    }

    @GameTest
    public static void testDependency(GameTestHelper helper) {
        if (DependencyChecker.runRuntimeCheck(helper.getLevel())) {
            helper.succeed();
        } else {
            helper.fail("Tinactory dependency checker failed");
        }
    }

    @GameTest(timeoutTicks = 40)
    public static void testMockPlayerUseCreatesMachineNetwork(GameTestHelper helper) {
        var machinePos = new BlockPos(1, 1, 1);
        helper.setBlock(machinePos, machineState(Direction.EAST));
        useWithTeamMockPlayer(helper, machinePos);
        helper.runAfterDelay(12, () -> {
            var manager = WorldNetworkManagers.get(helper.getLevel());
            var absoluteMachinePos = helper.absolutePos(machinePos);
            if (manager.getNetworkAtPos(absoluteMachinePos).isEmpty()) {
                helper.fail("Mock player use did not create a ticking machine network", machinePos);
            }
            helper.succeed();
        });
    }

    @GameTest(timeoutTicks = 40)
    public static void testNetworkConnectivity(GameTestHelper helper) {
        var machinePos = new BlockPos(1, 1, 1);
        var cablePos = machinePos.east();
        var subnetPos = cablePos.east();

        var cableState = componentBlock("cable").defaultBlockState()
            .setValue(CableBlock.EAST, true)
            .setValue(CableBlock.WEST, true);
        var subnetState = componentBlock("electric_buffer").defaultBlockState()
            .setValue(MachineBlock.IO_FACING, Direction.WEST);

        helper.setBlock(machinePos, machineState(Direction.EAST));
        helper.setBlock(cablePos, cableState);
        helper.setBlock(subnetPos, subnetState);
        useWithTeamMockPlayer(helper, machinePos);

        helper.runAfterDelay(12, () -> {
            var manager = WorldNetworkManagers.get(helper.getLevel());
            var absoluteMachinePos = helper.absolutePos(machinePos);
            var absoluteCablePos = helper.absolutePos(cablePos);
            var absoluteSubnetPos = helper.absolutePos(subnetPos);
            var maybeGraph = manager.getNetworkAtPos(absoluteMachinePos);
            if (maybeGraph.isEmpty()) {
                helper.fail("Machine was not discovered by the network", machinePos);
            }
            var graph = maybeGraph.orElseThrow();
            if (manager.getNetworkAtPos(absoluteCablePos).orElse(null) != graph) {
                helper.fail("Cable was not connected to the machine network", cablePos);
            }
            if (manager.getNetworkAtPos(absoluteSubnetPos).orElse(null) != graph) {
                helper.fail("Electric buffer was not connected to the machine network", subnetPos);
            }
            var network = MACHINE.tryGet(helper.getBlockEntity(machinePos))
                .flatMap(machine -> machine.network())
                .orElseThrow();
            if (!network.getSubnet(absoluteCablePos, ELECTRIC_SUBNET.get()).equals(absoluteMachinePos)) {
                helper.fail("Cable did not inherit the machine subnet", cablePos);
            }
            if (!network.getSubnet(absoluteSubnetPos, ELECTRIC_SUBNET.get()).equals(absoluteMachinePos)) {
                helper.fail("Electric buffer did not inherit the parent subnet", subnetPos);
            }
            if (!network.getSubnet(absoluteCablePos, LOGISTICS_SUBNET.get()).equals(absoluteMachinePos)) {
                helper.fail("Cable did not inherit the logistics subnet", cablePos);
            }
            if (!network.getSubnet(absoluteSubnetPos, LOGISTICS_SUBNET.get()).equals(absoluteMachinePos)) {
                helper.fail("Electric buffer split the logistics subnet", subnetPos);
            }
            helper.succeed();
        });
    }

    @GameTest(timeoutTicks = 40)
    public static void testNetworkBridgeDoesNotCreateRootNetwork(GameTestHelper helper) {
        var bridgePos = new BlockPos(1, 1, 1);
        helper.setBlock(bridgePos, componentBlock("network_bridge").defaultBlockState()
            .setValue(MachineBlock.IO_FACING, Direction.EAST));
        useWithTeamMockPlayer(helper, bridgePos);

        helper.runAfterDelay(12, () -> {
            var manager = WorldNetworkManagers.get(helper.getLevel());
            var absoluteBridgePos = helper.absolutePos(bridgePos);
            if (manager.getNetworkAtPos(absoluteBridgePos).isPresent()) {
                helper.fail("Network bridge created a root network", bridgePos);
            }
            helper.succeed();
        });
    }

    @GameTest(timeoutTicks = 80)
    public static void testNetworkBridgeSplitsElectricAndLogisticsSubnets(GameTestHelper helper) {
        var parentMachinePos = new BlockPos(1, 1, 1);
        var parentCablePos = parentMachinePos.east();
        var bridgePos = parentCablePos.east();
        var childCablePos = bridgePos.east();
        var childMachinePos = childCablePos.east();

        helper.setBlock(parentMachinePos, machineState(Direction.EAST));
        helper.setBlock(parentCablePos, cableState(Voltage.LV, true, true));
        helper.setBlock(bridgePos, componentBlock("network_bridge").defaultBlockState()
            .setValue(MachineBlock.IO_FACING, Direction.EAST));
        helper.setBlock(childCablePos, cableState(Voltage.LV, true, true));
        helper.setBlock(childMachinePos, machineState(Direction.WEST));
        useWithTeamMockPlayer(helper, parentMachinePos);

        helper.runAfterDelay(16, () -> {
            var manager = WorldNetworkManagers.get(helper.getLevel());
            var absoluteParentMachinePos = helper.absolutePos(parentMachinePos);
            var absoluteBridgePos = helper.absolutePos(bridgePos);
            var absoluteChildCablePos = helper.absolutePos(childCablePos);
            var absoluteChildMachinePos = helper.absolutePos(childMachinePos);
            var graph = manager.getNetworkAtPos(absoluteParentMachinePos).orElseThrow();
            if (manager.getNetworkAtPos(absoluteBridgePos).orElse(null) != graph) {
                helper.fail("Network bridge was not connected to the parent network", bridgePos);
            }
            if (manager.getNetworkAtPos(absoluteChildMachinePos).orElse(null) != graph) {
                helper.fail("Child machine was not connected through the network bridge", childMachinePos);
            }
            var network = MACHINE.tryGet(helper.getBlockEntity(parentMachinePos))
                .flatMap(machine -> machine.network())
                .orElseThrow();
            if (!network.getSubnet(absoluteBridgePos, LOGISTICS_SUBNET.get()).equals(absoluteParentMachinePos)) {
                helper.fail("Network bridge did not inherit the parent logistics subnet", bridgePos);
            }
            if (!network.getSubnet(absoluteChildCablePos, LOGISTICS_SUBNET.get()).equals(absoluteBridgePos)) {
                helper.fail("Child cable did not inherit the bridge logistics subnet", childCablePos);
            }
            if (!network.getSubnet(absoluteChildMachinePos, LOGISTICS_SUBNET.get()).equals(absoluteBridgePos)) {
                helper.fail("Child machine did not inherit the bridge logistics subnet", childMachinePos);
            }
            if (!network.getSubnet(absoluteBridgePos, ELECTRIC_SUBNET.get()).equals(absoluteParentMachinePos)) {
                helper.fail("Network bridge did not inherit the parent electric subnet", bridgePos);
            }
            if (!network.getSubnet(absoluteChildCablePos, ELECTRIC_SUBNET.get()).equals(absoluteBridgePos)) {
                helper.fail("Child cable did not inherit the bridge electric subnet", childCablePos);
            }
            if (!network.getSubnet(absoluteChildMachinePos, ELECTRIC_SUBNET.get()).equals(absoluteBridgePos)) {
                helper.fail("Child machine did not inherit the bridge electric subnet", childMachinePos);
            }
            helper.succeed();
        });
    }

    @GameTest(timeoutTicks = 80)
    public static void testNetworkBridgeExposesParentStorageToChildWorkerAsBridgePort(GameTestHelper helper) {
        var parentStoragePos = new BlockPos(1, 1, 1);
        var parentCablePos = parentStoragePos.east();
        var bridgePos = parentCablePos.east();
        var childCablePos = bridgePos.east();
        var childWorkerPos = childCablePos.east();

        helper.setBlock(parentStoragePos, machineState("logistics/electric_chest", Voltage.MV, Direction.EAST));
        helper.setBlock(parentCablePos, cableState(Voltage.MV, true, true));
        helper.setBlock(bridgePos, componentBlock("network_bridge", Voltage.MV).defaultBlockState()
            .setValue(MachineBlock.IO_FACING, Direction.EAST));
        helper.setBlock(childCablePos, cableState(Voltage.MV, true, true));
        helper.setBlock(childWorkerPos, machineState("logistics/logistic_worker", Voltage.MV, Direction.WEST));
        useWithTeamMockPlayer(helper, parentStoragePos);

        helper.runAfterDelay(24, () -> {
            var network = MACHINE.tryGet(helper.getBlockEntity(parentStoragePos))
                .flatMap(machine -> machine.network())
                .orElseThrow();
            var logistics = network.getComponent(LOGISTIC_COMPONENT.get());
            var bridge = MACHINE.get(helper.getBlockEntity(bridgePos));
            var parentStorage = MACHINE.get(helper.getBlockEntity(parentStoragePos));
            var childWorker = MACHINE.get(helper.getBlockEntity(childWorkerPos));
            var visible = logistics.getVisiblePorts(childWorker);
            if (visible.stream().noneMatch(info -> info.machine().uuid().equals(bridge.uuid()) &&
                info.portIndex() == 0)) {
                helper.fail("Child worker did not see parent item storage through bridge port", childWorkerPos);
            }
            if (visible.stream().anyMatch(info -> info.machine().uuid().equals(parentStorage.uuid()))) {
                helper.fail("Child worker saw parent storage directly instead of through bridge port", childWorkerPos);
            }
            helper.succeed();
        });
    }

    @GameTest(timeoutTicks = 80)
    public static void testNetworkBridgePortsAreExcludedFromStorageAggregation(GameTestHelper helper) {
        var parentWorkerPos = new BlockPos(1, 1, 1);
        var parentCablePos = parentWorkerPos.east();
        var bridgePos = parentCablePos.east();
        var childCablePos = bridgePos.east();
        var childStoragePos = childCablePos.east();

        helper.setBlock(parentWorkerPos, machineState("logistics/logistic_worker", Voltage.MV, Direction.EAST));
        helper.setBlock(parentCablePos, cableState(Voltage.MV, true, true));
        helper.setBlock(bridgePos, componentBlock("network_bridge", Voltage.MV).defaultBlockState()
            .setValue(MachineBlock.IO_FACING, Direction.EAST));
        helper.setBlock(childCablePos, cableState(Voltage.MV, true, true));
        helper.setBlock(childStoragePos, machineState("logistics/electric_chest", Voltage.MV, Direction.WEST));
        useWithTeamMockPlayer(helper, parentWorkerPos);

        helper.runAfterDelay(24, () -> {
            var network = MACHINE.tryGet(helper.getBlockEntity(parentWorkerPos))
                .flatMap(machine -> machine.network())
                .orElseThrow();
            var logistics = network.getComponent(LOGISTIC_COMPONENT.get());
            var bridge = MACHINE.get(helper.getBlockEntity(bridgePos));
            var childStorage = MACHINE.get(helper.getBlockEntity(childStoragePos));
            var parentWorker = MACHINE.get(helper.getBlockEntity(parentWorkerPos));
            var visible = logistics.getVisiblePorts(parentWorker);
            if (visible.stream().noneMatch(info -> info.machine().uuid().equals(bridge.uuid()) &&
                info.portIndex() == 2)) {
                helper.fail("Parent worker did not see child item storage through bridge port", parentWorkerPos);
            }
            if (visible.stream().anyMatch(info -> info.machine().uuid().equals(childStorage.uuid()))) {
                helper.fail("Parent worker saw child storage directly instead of through bridge port", parentWorkerPos);
            }
            if (!logistics.getStoragePorts(parentWorker).isEmpty()) {
                helper.fail("Parent storage aggregation included bridge ports", parentWorkerPos);
            }
            helper.succeed();
        });
    }

    @GameTest(timeoutTicks = 80)
    public static void testBatteryBoxPowersElectricConsumer(GameTestHelper helper) {
        var voltage = Voltage.MV;
        var consumerPos = new BlockPos(0, 1, 1);
        var cablePos = consumerPos.east();
        var batteryBoxPos = cablePos.east();

        var cableState = componentBlock("cable", voltage).defaultBlockState()
            .setValue(CableBlock.EAST, true)
            .setValue(CableBlock.WEST, true);
        var batteryBoxState = machineState("battery_box", voltage, Direction.WEST)
            .setValue(MachineBlock.IO_FACING, Direction.WEST);

        helper.setBlock(consumerPos, machineState("logistics/electric_chest", voltage, Direction.EAST));
        helper.setBlock(cablePos, cableState);
        helper.setBlock(batteryBoxPos, batteryBoxState);
        var battery = batteryItem(voltage);
        var batteryStack = new ItemStack(battery);
        battery.setPower(batteryStack, battery.capacity);
        var batteryBoxEntity = helper.getBlockEntity(batteryBoxPos);
        var batteryMachine = MACHINE.get(batteryBoxEntity);
        batteryMachine.config().apply(SetMachineConfigPacket.builder()
            .set(DISCHARGE_KEY, true)
            .get());
        MENU_ITEM_HANDLER.get(batteryBoxEntity).asItemHandler().insertItem(0, batteryStack, false);
        useWithTeamMockPlayer(helper, consumerPos);

        helper.runAfterDelay(24, () -> {
            var network = MACHINE.tryGet(helper.getBlockEntity(consumerPos))
                .flatMap(machine -> machine.network())
                .orElseThrow();
            var electric = network.getComponent(ELECTRIC_COMPONENT.get());
            var consumer = ELECTRIC_MACHINE.get(helper.getBlockEntity(consumerPos));
            var generator = ELECTRIC_MACHINE.get(batteryBoxEntity);
            if (consumer.getMachineType() != ElectricMachineType.CONSUMER || consumer.getPowerCons() <= 0d) {
                helper.fail("Electric storage did not expose consumer demand", consumerPos);
            }
            if (generator.getMachineType() != ElectricMachineType.GENERATOR || generator.getPowerGen() <= 0d) {
                helper.fail("Battery box did not expose charged battery generation", batteryBoxPos);
            }
            if (electric.getWorkFactor() <= 0d) {
                helper.fail("Battery box did not power the electric consumer", consumerPos);
            }
            if (battery.getPower(batteryStack) >= battery.capacity) {
                helper.fail("Battery box did not consume stored battery power", batteryBoxPos);
            }
            helper.succeed();
        });
    }

    @GameTest
    public static void testDigitalInterfaceReserveBuffers(GameTestHelper helper) {
        var interfacePos = new BlockPos(1, 1, 1);
        helper.setBlock(interfacePos, machineState("multiblock/digital_interface", Voltage.EV, Direction.NORTH));
        var digitalInterface = (DigitalInterface) MACHINE.get(helper.getBlockEntity(interfacePos));
        digitalInterface.setLayout(Layout.builder()
            .slot(0, SlotType.ITEM_INPUT, 0, 0, Voltage.between(Voltage.EV, Voltage.EV))
            .slot(1, SlotType.ITEM_INPUT, 18, 0, Voltage.between(Voltage.EV, Voltage.EV))
            .slot(2, SlotType.ITEM_OUTPUT, 36, 0, Voltage.between(Voltage.EV, Voltage.EV))
            .buildLayout(Voltage.EV));

        var firstInput = digitalInterface.getPort(0, ContainerAccess.EXTERNAL).asItem();
        var secondInput = digitalInterface.getPort(1, ContainerAccess.EXTERNAL).asItem();
        var output = digitalInterface.getPort(2, ContainerAccess.INTERNAL).asItem();

        assertInserted(helper, interfacePos, firstInput.insert(stack(Items.COBBLESTONE, 2048), false), 2048, 1072,
            "first input type should use one input reserve slot plus shared capacity");
        assertInserted(helper, interfacePos, secondInput.insert(stack(Items.DIRT, 2048), false), 2048, 48,
            "second input type should still fit in a separate reserve slot after shared capacity is full");
        assertInserted(helper, interfacePos, output.insert(stack(Items.IRON_INGOT, 2048), false), 2048, 496,
            "output should use the output reserve inside total displayed capacity");

        helper.succeed();
    }

    @GameTest(template = "empty_20x5x5", timeoutTicks = 160)
    public static void testTransformerPowersOversizedEvConsumer(GameTestHelper helper) {
        var batteryBoxPos = new BlockPos(0, 1, 1);
        var hvCablePos = batteryBoxPos.east();
        var transformerPos = hvCablePos.east();
        var evCableStartPos = transformerPos.east();
        var consumerCount = 17;

        helper.setBlock(batteryBoxPos, machineState("battery_box", Voltage.HV, Direction.EAST));
        helper.setBlock(hvCablePos, cableState(Voltage.HV, true, true));
        helper.setBlock(transformerPos, componentBlock("transformer", Voltage.EV).defaultBlockState()
            .setValue(MachineBlock.IO_FACING, Direction.EAST));
        for (var i = 0; i < consumerCount; i++) {
            var cablePos = evCableStartPos.east(i);
            var consumerPos = cablePos.south();
            helper.setBlock(cablePos, cableState(Voltage.EV, true, true)
                .setValue(CableBlock.SOUTH, true));
            helper.setBlock(consumerPos, machineState("logistics/logistic_worker", Voltage.EV, Direction.NORTH));
        }

        var battery = batteryItem(Voltage.HV);
        var batteryStack = new ItemStack(battery);
        battery.setPower(batteryStack, battery.capacity);
        var batteryBoxEntity = helper.getBlockEntity(batteryBoxPos);
        MENU_ITEM_HANDLER.get(batteryBoxEntity).asItemHandler().insertItem(0, batteryStack, false);
        useWithTeamMockPlayer(helper, batteryBoxPos);

        helper.runAfterDelay(80, () -> {
            var network = MACHINE.tryGet(helper.getBlockEntity(batteryBoxPos))
                .flatMap(machine -> machine.network())
                .orElseThrow();
            var electric = network.getComponent(ELECTRIC_COMPONENT.get());
            var consumerPower = 0d;
            for (var i = 0; i < consumerCount; i++) {
                var consumerPos = evCableStartPos.east(i).south();
                var consumer = ELECTRIC_MACHINE.get(helper.getBlockEntity(consumerPos));
                if (consumer.getMachineType() != ElectricMachineType.CONSUMER) {
                    helper.fail("EV consumer was not active", consumerPos);
                }
                consumerPower += consumer.getPowerCons();
            }
            if (consumerPower <= Voltage.HV.value) {
                helper.fail("EV consumers did not exceed one HV amp", evCableStartPos.south());
            }
            var batteryPower = battery.getPower(batteryStack);
            var factorReport = "workFactor=" + electric.getWorkFactor() +
                ", bufferFactor=" + electric.getBufferFactor() +
                ", consumerPower=" + consumerPower +
                ", batteryPower=" + batteryPower;
            if (electric.getWorkFactor() <= 0d || electric.getWorkFactor() >= 1d) {
                helper.fail("HV buffer through transformer should partially power oversized EV consumers: " +
                        factorReport,
                    evCableStartPos.south());
            }
            if (electric.getBufferFactor() >= 0d) {
                helper.fail("HV battery box buffer did not discharge through transformer: " + factorReport,
                    batteryBoxPos);
            }
            if (batteryPower >= battery.capacity) {
                helper.fail("HV battery was not drained through transformer: " + factorReport, batteryBoxPos);
            }
            helper.succeed();
        });
    }

    private static ItemStack stack(Item item, int count) {
        var ret = new ItemStack(item);
        ret.setCount(count);
        return ret;
    }

    private static void assertInserted(GameTestHelper helper, BlockPos pos, ItemStack remaining, int requested,
        int expected, String message) {
        var inserted = requested - remaining.getCount();
        if (inserted != expected) {
            helper.fail(message + ": expected " + expected + ", got " + inserted, pos);
        }
    }

    private static BlockState machineState(Direction ioFacing) {
        return machineState("electric_furnace", ioFacing);
    }

    private static BlockState machineState(String name, Direction ioFacing) {
        return machineState(name, Voltage.LV, ioFacing);
    }

    private static BlockState machineState(String name, Voltage voltage, Direction ioFacing) {
        return Objects.requireNonNull(AllBlockEntities.getMachine(name), name)
            .block(voltage)
            .defaultBlockState()
            .setValue(MachineBlock.IO_FACING, ioFacing);
    }

    private static Block componentBlock(String name) {
        return componentBlock(name, Voltage.LV);
    }

    private static Block componentBlock(String name, Voltage voltage) {
        return (Block) AllItems.getComponent(name).get(voltage).get();
    }

    private static BlockState cableState(Voltage voltage, boolean east, boolean west) {
        return componentBlock("cable", voltage).defaultBlockState()
            .setValue(CableBlock.EAST, east)
            .setValue(CableBlock.WEST, west);
    }

    private static BatteryItem batteryItem(Voltage voltage) {
        return (BatteryItem) AllItems.getComponent("battery").get(voltage).get();
    }

    private static void useWithTeamMockPlayer(GameTestHelper helper, BlockPos pos) {
        var player = helper.makeMockPlayer();
        var scoreboard = helper.getLevel().getScoreboard();
        var team = scoreboard.getPlayerTeam(TEAM_NAME);
        assert team != null;
        scoreboard.addPlayerToTeam(player.getScoreboardName(), team);

        var absolutePos = helper.absolutePos(pos);
        var state = helper.getLevel().getBlockState(absolutePos);
        state.use(helper.getLevel(), player, InteractionHand.MAIN_HAND, new BlockHitResult(
            Vec3.atCenterOf(absolutePos), Direction.NORTH, absolutePos, true));
    }
}
