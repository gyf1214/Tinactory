package org.shsts.tinactory.gametest.create;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.shsts.tinactory.AllItems;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.compat.create.CreateElectricMotorBlockEntity;
import org.shsts.tinactory.compat.create.CreateKineticGeneratorBlockEntity;
import org.shsts.tinactory.content.electric.BatteryBoxMode;
import org.shsts.tinactory.content.tool.PoweredItem;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.integration.network.CableBlock;
import org.shsts.tinactory.integration.network.MachineBlock;

import java.util.List;
import java.util.Objects;

import static org.shsts.tinactory.AllBlockEntities.getMachine;
import static org.shsts.tinactory.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.AllCapabilities.EVENT_MANAGER;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllCapabilities.MENU_ITEM_HANDLER;
import static org.shsts.tinactory.AllNetworks.BATTERY_MODE;
import static org.shsts.tinactory.AllNetworks.ELECTRIC_COMPONENT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class CreateIntegrationGameTest {
    private static final BlockPos MOTOR_A = new BlockPos(1, 2, 2);
    private static final BlockPos MOTOR_B = new BlockPos(5, 2, 2);
    private static final BlockPos BATTERY = new BlockPos(3, 2, 4);

    private CreateIntegrationGameTest() {}

    @GameTest(template = "empty_16x8x8", templateNamespace = TinactoryKeys.ID)
    public static void testSixConverterRegistrationsAndContainers(GameTestHelper helper) {
        var voltages = List.of(Voltage.ULV, Voltage.MV, Voltage.EV);
        for (var i = 0; i < voltages.size(); i++) {
            var voltage = voltages.get(i);
            var motorPos = new BlockPos(1 + i, 2, 2);
            var generatorPos = new BlockPos(5 + i, 2, 2);
            var motorBlock = getMachine("electric_motor").block(voltage);
            var generatorBlock = getMachine("kinetic_generator").block(voltage);
            if (!(motorBlock instanceof IRotate) || !(generatorBlock instanceof IRotate)) {
                helper.fail("A converter block does not expose Create shaft geometry", motorPos);
                return;
            }
            helper.setBlock(motorPos, motorBlock.defaultBlockState());
            helper.setBlock(generatorPos, generatorBlock.defaultBlockState());
            var motor = helper.getBlockEntity(motorPos);
            var generator = helper.getBlockEntity(generatorPos);
            if (!(motor instanceof CreateElectricMotorBlockEntity) ||
                !(generator instanceof CreateKineticGeneratorBlockEntity) ||
                EVENT_MANAGER.tryGet(motor).isEmpty() ||
                EVENT_MANAGER.tryGet(generator).isEmpty() ||
                MACHINE.tryGet(motor).isEmpty() ||
                MACHINE.tryGet(generator).isEmpty() ||
                ELECTRIC_MACHINE.tryGet(motor).isEmpty() ||
                ELECTRIC_MACHINE.tryGet(generator).isEmpty()) {
                helper.fail("A converter tier did not register its Create entity and Tinactory containers", motorPos);
                return;
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty_16x8x8", templateNamespace = TinactoryKeys.ID, timeoutTicks = 120)
    public static void testPartialElectricSupplyScalesMotorCapacity(GameTestHelper helper) {
        placePoweredMotors(helper, Voltage.MV, 1);
        helper.runAfterDelay(24, () -> {
            var first = motor(helper, MOTOR_A);
            var second = motor(helper, MOTOR_B);
            var network = MACHINE.get(helper.getBlockEntity(MOTOR_A)).network().orElse(null);
            if (network == null || network.getComponent(ELECTRIC_COMPONENT.get()).getWorkFactor() < 0.49d ||
                network.getComponent(ELECTRIC_COMPONENT.get()).getWorkFactor() > 0.51d) {
                helper.fail("One MV battery did not partially supply two motors", MOTOR_A);
                return;
            }
            if (ELECTRIC_MACHINE.get(helper.getBlockEntity(MOTOR_A)).getPowerCons() != 128d ||
                ELECTRIC_MACHINE.get(helper.getBlockEntity(MOTOR_B)).getPowerCons() != 128d) {
                helper.fail("Partial supply changed a motor's fixed electrical draw", MOTOR_A);
                return;
            }
            var workFactor = network.getComponent(ELECTRIC_COMPONENT.get()).getWorkFactor();
            var expectedCapacity = 3_200f * 32f * (float) workFactor;
            if (Math.abs(first.getSpeed()) != 32f || Math.abs(second.getSpeed()) != 32f ||
                Math.abs(first.getOrCreateNetwork().getActualCapacityOf(first) - expectedCapacity) > 1f ||
                Math.abs(second.getOrCreateNetwork().getActualCapacityOf(second) - expectedCapacity) > 1f) {
                helper.fail("Partial supply mismatch: workFactor=" +
                    workFactor + ", speed=" +
                    first.getSpeed() + "/" + second.getSpeed() + ", capacity=" +
                    first.getOrCreateNetwork().getActualCapacityOf(first) + "/" +
                    second.getOrCreateNetwork().getActualCapacityOf(second), MOTOR_A);
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty_8x8x8", templateNamespace = TinactoryKeys.ID)
    public static void testUnpoweredMotorStopsButRetainsFixedDemand(GameTestHelper helper) {
        var motorPos = new BlockPos(2, 2, 2);
        var cablePos = motorPos.south();
        helper.setBlock(motorPos, motorState(Voltage.ULV, Direction.EAST, Direction.SOUTH));
        helper.setBlock(motorPos.east(), shaftState());
        helper.setBlock(cablePos, cableState(Voltage.ULV, true, false, false, false));
        useWithMockPlayer(helper, motorPos);
        helper.runAfterDelay(24, () -> {
            var motor = motor(helper, motorPos);
            var network = MACHINE.get(helper.getBlockEntity(motorPos)).network().orElseThrow();
            if (network.getComponent(ELECTRIC_COMPONENT.get()).getWorkFactor() != 0d ||
                ELECTRIC_MACHINE.get(helper.getBlockEntity(motorPos)).getPowerCons() != 8d ||
                motor.getSpeed() != 0f) {
                helper.fail("An unpowered motor kept rotating or stopped requesting its fixed rating", motorPos);
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty_12x8x8", templateNamespace = TinactoryKeys.ID, timeoutTicks = 120)
    public static void testMotorDemandDoesNotChangeWhenKineticLoadIsAdded(GameTestHelper helper) {
        var motorPos = new BlockPos(2, 2, 2);
        var cablePos = motorPos.south();
        var batteryPos = cablePos.south();
        var voltage = Voltage.MV;
        helper.setBlock(motorPos, motorState(voltage, Direction.EAST, Direction.SOUTH));
        helper.setBlock(motorPos.east(), shaftState());
        helper.setBlock(cablePos, cableState(voltage, true, true, false, false));
        helper.setBlock(batteryPos, machineState("battery_box", voltage, Direction.NORTH));
        addBattery(helper, batteryPos, voltage, 1);
        useWithMockPlayer(helper, motorPos);
        helper.runAfterDelay(12, () -> {
            var motor = motor(helper, motorPos);
            var demandBeforeLoad = ELECTRIC_MACHINE.get(helper.getBlockEntity(motorPos)).getPowerCons();
            helper.setBlock(motorPos.east(2),
                getMachine("kinetic_generator").block(Voltage.ULV).defaultBlockState()
                    .setValue(MachineBlock.FACING, Direction.WEST));
            helper.runAfterDelay(16, () -> {
                var generator = helper.getBlockEntity(motorPos.east(2));
                var demandAfterLoad = ELECTRIC_MACHINE.get(helper.getBlockEntity(motorPos)).getPowerCons();
                if (!(generator instanceof CreateKineticGeneratorBlockEntity) ||
                    Math.abs(motor.getSpeed()) != 32f || demandBeforeLoad != 128d ||
                    demandAfterLoad != demandBeforeLoad ||
                    Math.abs(motor.getOrCreateNetwork().calculateStress()) <= 0f) {
                    helper.fail("Adding a Create kinetic load changed the motor's fixed draw or did not load its shaft",
                        motorPos);
                    return;
                }
                helper.succeed();
            });
        });
    }

    @GameTest(template = "empty_12x8x8", templateNamespace = TinactoryKeys.ID, timeoutTicks = 160)
    public static void testGeneratorTracksSpeedImpactAndRatedOutputCap(GameTestHelper helper) {
        var sourcePos = new BlockPos(2, 2, 2);
        var generatorPos = sourcePos.east();
        helper.setBlock(sourcePos, AllBlocks.CREATIVE_MOTOR.get().defaultBlockState()
            .setValue(DirectionalKineticBlock.FACING, Direction.EAST));
        helper.setBlock(generatorPos, getMachine("kinetic_generator").block(Voltage.ULV).defaultBlockState()
            .setValue(MachineBlock.FACING, Direction.WEST));
        var source = (CreativeMotorBlockEntity) helper.getBlockEntity(sourcePos);
        var generator = generator(helper, generatorPos);
        source.generatedSpeed.setValue(16);
        helper.runAfterDelay(12, () -> {
            if (!assertGenerator(helper, generatorPos, generator, 16f, 8d, 16_384f)) {
                return;
            }
            source.generatedSpeed.setValue(32);
            helper.runAfterDelay(12, () -> {
                if (!assertGenerator(helper, generatorPos, generator, 32f, 8d, 32_768f)) {
                    return;
                }
                source.generatedSpeed.setValue(0);
                helper.runAfterDelay(12, () -> {
                    if (!assertGenerator(helper, generatorPos, generator, 0f, 0d, 0f)) {
                        return;
                    }
                    helper.succeed();
                });
            });
        });
    }

    @GameTest(template = "empty_16x8x8", templateNamespace = TinactoryKeys.ID, timeoutTicks = 160)
    public static void testCreateSharesCapacityAcrossTinactoryMotorSources(GameTestHelper helper) {
        var voltage = Voltage.MV;
        var firstPos = new BlockPos(1, 2, 2);
        var secondPos = new BlockPos(3, 2, 2);
        var shaftPos = new BlockPos(2, 2, 2);
        var batteryPos = new BlockPos(2, 2, 5);
        helper.setBlock(firstPos, motorState(voltage, Direction.EAST, Direction.SOUTH));
        helper.setBlock(secondPos, motorState(voltage, Direction.WEST, Direction.SOUTH));
        helper.setBlock(shaftPos, shaftState());
        helper.setBlock(firstPos.south(), cableState(voltage, true, false, true, false));
        helper.setBlock(shaftPos.south(), cableState(voltage, false, true, true, true));
        helper.setBlock(secondPos.south(), cableState(voltage, false, true, false, true));
        helper.setBlock(new BlockPos(2, 2, 4), cableState(voltage, true, true, false, false));
        helper.setBlock(batteryPos, machineState("battery_box", voltage, Direction.NORTH));
        addBattery(helper, batteryPos, voltage, 2);
        useWithMockPlayer(helper, firstPos);
        useWithMockPlayer(helper, secondPos);
        helper.runAfterDelay(30, () -> {
            var first = motor(helper, firstPos);
            var second = motor(helper, secondPos);
            var network = first.getOrCreateNetwork();
            var capacity = network.calculateCapacity();
            var firstCapacity = network.getActualCapacityOf(first);
            var secondCapacity = network.getActualCapacityOf(second);
            if (Math.abs(first.getSpeed()) != 32f ||
                Math.abs(second.getSpeed()) != 32f ||
                !Objects.equals(first.network, second.network) ||
                Math.abs(capacity - firstCapacity - secondCapacity) > 2f ||
                network.sources.size() != 2) {
                helper.fail("Create capacity pooling mismatch: speed=" + first.getSpeed() + "/" + second.getSpeed() +
                    ", capacity=" + capacity + ", sourceCapacities=" + firstCapacity + "/" + secondCapacity +
                    ", sharedNetwork=" + Objects.equals(first.network, second.network) + ", sourceCount=" +
                    network.sources.size(), shaftPos);
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty_12x8x8", templateNamespace = TinactoryKeys.ID, timeoutTicks = 160)
    public static void testCreateOverstressStopsGeneratorAndContainerLifecycle(GameTestHelper helper) {
        var motorPos = new BlockPos(2, 2, 2);
        var generatorPos = motorPos.east(2);
        var cablePos = motorPos.south();
        var batteryPos = cablePos.south();
        var voltage = Voltage.MV;
        helper.setBlock(motorPos, motorState(voltage, Direction.EAST, Direction.SOUTH));
        helper.setBlock(motorPos.east(), shaftState());
        helper.setBlock(generatorPos, getMachine("kinetic_generator").block(Voltage.EV).defaultBlockState()
            .setValue(MachineBlock.FACING, Direction.WEST));
        helper.setBlock(cablePos, cableState(voltage, true, true, false, false));
        helper.setBlock(batteryPos, machineState("battery_box", voltage, Direction.NORTH));
        addBattery(helper, batteryPos, voltage, 1);
        useWithMockPlayer(helper, motorPos);
        helper.runAfterDelay(30, () -> {
            var motor = motor(helper, motorPos);
            var generator = generator(helper, generatorPos);
            var kineticNetwork = motor.getOrCreateNetwork();
            var capacity = kineticNetwork.calculateCapacity();
            var stress = kineticNetwork.calculateStress();
            var sourceMachine = MACHINE.get(helper.getBlockEntity(motorPos));
            var oldId = sourceMachine.uuid();
            var workFactor = sourceMachine.network().orElseThrow()
                .getComponent(ELECTRIC_COMPONENT.get()).getWorkFactor();
            var expectedCapacity = 102_400f * (float) workFactor;
            var provider = helper.getLevel().registryAccess();
            var saved = motor.saveWithoutMetadata(provider);
            var restoredPos = new BlockPos(8, 2, 2);
            helper.setBlock(restoredPos, motorState(voltage, Direction.EAST, Direction.SOUTH));
            var restored = helper.getBlockEntity(restoredPos);
            restored.loadCustomOnly(saved, provider);
            var packet = motor.getUpdatePacket();
            var generatorNetwork = generator.hasNetwork() ? generator.getOrCreateNetwork() : null;
            var generatorStress = generatorNetwork == null ? 0f : generatorNetwork.getActualStressOf(generator);
            var restoredIdMatches = MACHINE.get(restored).uuid().equals(oldId);
            var hasEventManager = EVENT_MANAGER.tryGet(restored).isPresent();
            if (Math.abs(capacity - expectedCapacity) > 2f || Math.abs(stress - 1_605_632f) > 2f ||
                !motor.isOverStressed() || Math.abs(generator.getSpeed()) != 0f ||
                ELECTRIC_MACHINE.get(helper.getBlockEntity(generatorPos)).getPowerGen() != 0d ||
                !restoredIdMatches || packet == null || !hasEventManager) {
                helper.fail("Create overstress/lifecycle mismatch: capacity=" + capacity + "/" + expectedCapacity +
                    ", stress=" + stress +
                    ", overStressed=" + motor.isOverStressed() + ", generatorSpeed=" + generator.getSpeed() +
                    ", generatorPower=" + ELECTRIC_MACHINE.get(helper.getBlockEntity(generatorPos)).getPowerGen() +
                    ", generatorHasNetwork=" + generator.hasNetwork() + ", generatorStress=" + generatorStress +
                    ", restoredId=" + restoredIdMatches + ", packet=" + (packet != null) +
                    ", eventManager=" + hasEventManager, motorPos);
                return;
            }
            helper.destroyBlock(motorPos);
            helper.runAfterDelay(4, () -> {
                var remainedInNetwork = sourceMachine.network()
                    .map(network -> network.allMachines().contains(sourceMachine)).orElse(false);
                if (remainedInNetwork) {
                    helper.fail("Removing a converter left it registered in its Tinactory electric network", motorPos);
                    return;
                }
                helper.succeed();
            });
        });
    }

    private static boolean assertGenerator(GameTestHelper helper, BlockPos pos,
        CreateKineticGeneratorBlockEntity generator, float speed, double power, float stress) {
        var actualSpeed = Math.abs(generator.getSpeed());
        var actualPower = ELECTRIC_MACHINE.get(helper.getBlockEntity(pos)).getPowerGen();
        var actualStress = generator.hasNetwork() ?
            generator.getOrCreateNetwork().getActualStressOf(generator) : 0f;
        if (actualSpeed != speed || Math.abs(actualPower - power) > 0.001d ||
            Math.abs(actualStress - stress) > 1f) {
            helper.fail("Generator curve mismatch: speed=" + actualSpeed + ", power=" + actualPower +
                ", stress=" + actualStress + ", expected=" + speed + "/" + power + "/" + stress, pos);
            return false;
        }
        return true;
    }

    private static void placePoweredMotors(GameTestHelper helper, Voltage voltage, int batteryCount) {
        helper.setBlock(MOTOR_A, motorState(voltage, Direction.EAST, Direction.SOUTH));
        helper.setBlock(MOTOR_B, motorState(voltage, Direction.WEST, Direction.SOUTH));
        helper.setBlock(MOTOR_A.east(), shaftState());
        helper.setBlock(MOTOR_B.west(), shaftState());
        for (var x = 1; x <= 5; x++) {
            helper.setBlock(new BlockPos(x, 2, 3), cableState(voltage,
                x == 1 || x == 5, x == 3, x < 5, x > 1));
        }
        helper.setBlock(BATTERY, machineState("battery_box", voltage, Direction.NORTH));
        addBattery(helper, BATTERY, voltage, batteryCount);
        useWithMockPlayer(helper, MOTOR_A);
        useWithMockPlayer(helper, MOTOR_B);
    }

    private static void addBattery(GameTestHelper helper, BlockPos pos, Voltage voltage, int count) {
        var battery = (PoweredItem) AllItems.getComponent("battery").get(voltage).get();
        var machine = MACHINE.get(helper.getBlockEntity(pos));
        machine.config().apply(SetMachineConfigPacket.builder().set(BATTERY_MODE, BatteryBoxMode.DISCHARGE).get());
        for (var i = 0; i < count; i++) {
            var stack = new ItemStack(battery);
            battery.setPower(stack, battery.capacity());
            MENU_ITEM_HANDLER.get(helper.getBlockEntity(pos)).insertItem(i, stack, false);
        }
    }

    private static BlockState motorState(Voltage voltage, Direction facing, Direction ioFacing) {
        return getMachine("electric_motor").block(voltage).defaultBlockState()
            .setValue(MachineBlock.FACING, facing)
            .setValue(MachineBlock.IO_FACING, ioFacing);
    }

    private static BlockState machineState(String id, Voltage voltage, Direction ioFacing) {
        return getMachine(id).block(voltage).defaultBlockState()
            .setValue(MachineBlock.IO_FACING, ioFacing);
    }

    private static BlockState cableState(Voltage voltage,
        boolean north, boolean south, boolean east, boolean west) {
        return componentBlock("cable", voltage).defaultBlockState()
            .setValue(CableBlock.NORTH, north)
            .setValue(CableBlock.SOUTH, south)
            .setValue(CableBlock.EAST, east)
            .setValue(CableBlock.WEST, west);
    }

    private static Block componentBlock(String id, Voltage voltage) {
        return (Block) AllItems.getComponent(id).get(voltage).get();
    }

    private static BlockState shaftState() {
        return AllBlocks.SHAFT.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
    }

    private static CreateElectricMotorBlockEntity motor(GameTestHelper helper, BlockPos pos) {
        return helper.getBlockEntity(pos);
    }

    private static CreateKineticGeneratorBlockEntity generator(GameTestHelper helper, BlockPos pos) {
        return helper.getBlockEntity(pos);
    }

    private static void useWithMockPlayer(GameTestHelper helper, BlockPos pos) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        useWithPlayer(helper, player, pos);
    }

    private static void useWithPlayer(GameTestHelper helper, Player player, BlockPos pos) {
        var absolutePos = helper.absolutePos(pos);
        var state = helper.getLevel().getBlockState(absolutePos);
        state.useItemOn(ItemStack.EMPTY, helper.getLevel(), player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(absolutePos), Direction.NORTH, absolutePos, true));
    }
}
