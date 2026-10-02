package org.shsts.tinactory.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
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
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.tool.IPoweredItem;
import org.shsts.tinactory.content.electric.BatteryBoxMode;
import org.shsts.tinactory.content.tool.PoweredItem;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.integration.network.CableBlock;
import org.shsts.tinactory.integration.network.MachineBlock;

import java.util.Objects;

import static org.shsts.tinactory.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllCapabilities.MENU_ITEM_HANDLER;
import static org.shsts.tinactory.AllNetworks.BATTERY_MODE;
import static org.shsts.tinactory.AllNetworks.ELECTRIC_COMPONENT;

@GameTestHolder(TinactoryKeys.ID)
public final class BatteryBoxGameTest {
    @GameTest
    public static void testBatteryBoxAcceptsSameVoltagePoweredItems(GameTestHelper helper) {
        var boxPos = new BlockPos(1, 1, 1);
        helper.setBlock(boxPos, machineState(Voltage.LV, Direction.NORTH));
        var handler = MENU_ITEM_HANDLER.get(helper.getBlockEntity(boxPos));
        var battery = new ItemStack(batteryItem(Voltage.LV));
        var drill = new ItemStack(poweredItem("drill", Voltage.LV));
        var wrongVoltage = new ItemStack(batteryItem(Voltage.MV));

        if (!handler.insertItem(0, battery, false).isEmpty() ||
            !handler.insertItem(1, drill, false).isEmpty()) {
            helper.fail("Battery Box rejected a same-voltage powered item", boxPos);
            return;
        }
        if (!handler.insertItem(2, new ItemStack(Items.DIAMOND), false).is(Items.DIAMOND) ||
            !handler.insertItem(3, wrongVoltage, false).is(batteryItem(Voltage.MV))) {
            helper.fail("Battery Box accepted a non-powered or wrong-voltage item", boxPos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testChargeModeConsumesPowerAndChargesSlotsInParallel(GameTestHelper helper) {
        var sourcePos = new BlockPos(1, 1, 1);
        var cablePos = sourcePos.east();
        var chargePos = cablePos.east();
        helper.setBlock(sourcePos, machineState(Voltage.LV, Direction.EAST));
        helper.setBlock(cablePos, cableState(Voltage.LV, Direction.EAST, Direction.WEST));
        helper.setBlock(chargePos, machineState(Voltage.LV, Direction.WEST));

        var source = new ItemStack(batteryItem(Voltage.LV));
        batteryItem(Voltage.LV).setPower(source, batteryItem(Voltage.LV).capacity());
        MENU_ITEM_HANDLER.get(helper.getBlockEntity(sourcePos)).insertItem(0, source, false);
        setMode(helper, sourcePos, BatteryBoxMode.DISCHARGE);

        var battery = new ItemStack(batteryItem(Voltage.LV));
        var drillItem = poweredItem("drill", Voltage.LV);
        var poweredDrill = (IPoweredItem) drillItem;
        var drill = new ItemStack(drillItem);
        var chargeHandler = MENU_ITEM_HANDLER.get(helper.getBlockEntity(chargePos));
        chargeHandler.insertItem(0, battery, false);
        chargeHandler.insertItem(1, drill, false);
        setMode(helper, chargePos, BatteryBoxMode.CHARGE);
        useWithMockPlayer(helper, chargePos);

        helper.runAfterDelay(32, () -> {
            var chargeMachine = ELECTRIC_MACHINE.get(helper.getBlockEntity(chargePos));
            var network = MACHINE.get(helper.getBlockEntity(chargePos)).network().orElseThrow();
            var workFactor = network.getComponent(ELECTRIC_COMPONENT.get()).getWorkFactor();
            var batteryPower = batteryItem(Voltage.LV).getPower(battery);
            var drillPower = poweredDrill.getPower(drill);
            if (chargeMachine.getMachineType() != ElectricMachineType.CONSUMER ||
                workFactor <= 0d || workFactor >= 1d) {
                helper.fail("Charge mode did not consume power as an active network consumer", chargePos);
                return;
            }
            if (batteryPower <= 0L || batteryPower != drillPower) {
                helper.fail("Charge mode did not charge every eligible slot in parallel: battery=" + batteryPower +
                    ", drill=" + drillPower, chargePos);
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(timeoutTicks = 120)
    public static void testEmptyAndFullChargeBoxesDoNotReduceConsumerPower(GameTestHelper helper) {
        var sourcePos = new BlockPos(1, 1, 1);
        var cablePos = sourcePos.east();
        var chargePos = new BlockPos(2, 1, 0);
        var consumerPos = cablePos.east();
        helper.setBlock(sourcePos, machineState(Voltage.MV, Direction.EAST));
        helper.setBlock(chargePos, machineState(Voltage.MV, Direction.SOUTH));
        helper.setBlock(consumerPos,
            machineState("logistics/electric_chest", Voltage.MV, Direction.WEST));
        helper.setBlock(cablePos, cableState(Voltage.MV, Direction.WEST, Direction.EAST, Direction.NORTH));

        var source = new ItemStack(batteryItem(Voltage.MV));
        batteryItem(Voltage.MV).setPower(source, batteryItem(Voltage.MV).capacity());
        MENU_ITEM_HANDLER.get(helper.getBlockEntity(sourcePos)).insertItem(0, source, false);
        setMode(helper, sourcePos, BatteryBoxMode.DISCHARGE);
        setMode(helper, chargePos, BatteryBoxMode.CHARGE);
        useWithMockPlayer(helper, consumerPos);

        helper.runAfterDelay(24, () -> {
            var consumerMachine = MACHINE.get(helper.getBlockEntity(consumerPos));
            var network = consumerMachine.network().orElseThrow();
            var chargeMachine = ELECTRIC_MACHINE.get(helper.getBlockEntity(chargePos));
            var initialFactor = network.getComponent(ELECTRIC_COMPONENT.get()).getWorkFactor();
            if (chargeMachine.getMachineType() != ElectricMachineType.CONSUMER ||
                chargeMachine.getPowerCons() != 0d || initialFactor <= 0d) {
                helper.fail("An empty Charge-mode Battery Box reduced consumer power", chargePos);
                return;
            }

            var fullBattery = new ItemStack(batteryItem(Voltage.MV));
            batteryItem(Voltage.MV).setPower(fullBattery, batteryItem(Voltage.MV).capacity());
            MENU_ITEM_HANDLER.get(helper.getBlockEntity(chargePos)).insertItem(0, fullBattery, false);
            helper.runAfterDelay(24, () -> {
                var fullFactor = network.getComponent(ELECTRIC_COMPONENT.get()).getWorkFactor();
                if (chargeMachine.getPowerCons() != 0d || chargeMachine.getPowerGen() != 0d ||
                    Math.abs(fullFactor - initialFactor) > 0.0001d) {
                    helper.fail("A full Charge-mode item changed competing consumer power", chargePos);
                    return;
                }
                helper.succeed();
            });
        });
    }

    private static void setMode(GameTestHelper helper, BlockPos pos, BatteryBoxMode mode) {
        MACHINE.get(helper.getBlockEntity(pos)).config().apply(SetMachineConfigPacket.builder()
            .set(BATTERY_MODE, mode)
            .get());
    }

    private static BlockState machineState(Voltage voltage, Direction ioFacing) {
        return machineState("battery_box", voltage, ioFacing);
    }

    private static BlockState machineState(String name, Voltage voltage, Direction ioFacing) {
        return Objects.requireNonNull(AllBlockEntities.getMachine(name), name)
            .block(voltage)
            .defaultBlockState()
            .setValue(MachineBlock.IO_FACING, ioFacing);
    }

    private static BlockState cableState(Voltage voltage, Direction... directions) {
        var state = ((Block) AllItems.getComponent("cable").get(voltage).get()).defaultBlockState();
        for (var direction : directions) {
            state = state.setValue(CableBlock.PROPERTY_BY_DIRECTION.get(direction), true);
        }
        return state;
    }

    private static PoweredItem batteryItem(Voltage voltage) {
        return (PoweredItem) AllItems.getComponent("battery").get(voltage).get();
    }

    private static Item poweredItem(String family, Voltage voltage) {
        return (Item) AllItems.getComponent(family).get(voltage).get();
    }

    private static void useWithMockPlayer(GameTestHelper helper, BlockPos pos) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        useWithMockPlayer(helper, player, pos);
    }

    private static void useWithMockPlayer(GameTestHelper helper, Player player, BlockPos pos) {
        var absolutePos = helper.absolutePos(pos);
        var state = helper.getLevel().getBlockState(absolutePos);
        state.useItemOn(ItemStack.EMPTY, helper.getLevel(), player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(absolutePos), Direction.NORTH, absolutePos, true));
    }
}
