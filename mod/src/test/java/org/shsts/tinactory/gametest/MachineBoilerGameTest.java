package org.shsts.tinactory.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.shsts.tinactory.AllMaterials;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.api.logistics.ContainerAccess;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.machine.IBoiler;
import org.shsts.tinactory.integration.machine.Machine;
import org.shsts.tinactory.integration.network.MachineBlock;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllCapabilities.PROCESSOR;
import static org.shsts.tinactory.AllNetworks.SIGNAL_COMPONENT;

@GameTestHolder(TinactoryKeys.ID)
public final class MachineBoilerGameTest {
    private static final BlockPos BOILER_POS = new BlockPos(2, 2, 2);
    private static final Fluid STEAM = AllMaterials.getMaterial("water").fluid("gas").get();

    @GameTest(timeoutTicks = 80)
    public static void testBoilerFiltersFuelAndRespondsToStopSignal(GameTestHelper helper) {
        var machine = placeBoiler(helper);
        helper.runAfterDelay(12, () -> {
            var boiler = boiler(machine);
            var fuel = machine.container().orElseThrow().getPort(0, ContainerAccess.INTERNAL).asItem();
            var signal = machine.network().orElseThrow().getComponent(SIGNAL_COMPONENT.get());
            if (!signal.has(machine, machine.uuid(), Machine.STOP_SIGNAL, true)) {
                helper.fail("Boiler did not register its stop signal", BOILER_POS);
                return;
            }
            if (fuel.insert(new ItemStack(Items.DIRT), true).isEmpty() ||
                !fuel.insert(new ItemStack(Items.COAL_BLOCK), true).isEmpty()) {
                helper.fail("Boiler fuel filter accepted the wrong item", BOILER_POS);
                return;
            }

            signal.write(machine, machine.uuid(), Machine.STOP_SIGNAL, 1);
            fuel.insert(new ItemStack(Items.COAL_BLOCK), false);
            boiler.onPreWork();
            if (boiler.isWorking(0d)) {
                helper.fail("Stopped boiler consumed fuel", BOILER_POS);
                return;
            }
            signal.write(machine, machine.uuid(), Machine.STOP_SIGNAL, 0);
            boiler.onPreWork();
            if (!boiler.isWorking(0d)) {
                helper.fail("Boiler did not resume after its stop signal cleared", BOILER_POS);
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(timeoutTicks = 120)
    public static void testIdleBoilerKeepsWaterAndBaseHeat(GameTestHelper helper) {
        var machine = placeBoiler(helper);
        var input = machine.container().orElseThrow().getPort(1, ContainerAccess.INTERNAL).asFluid();
        input.insert(new FluidStack(Fluids.WATER, 1), false);

        helper.runAfterDelay(80, () -> {
            var boiler = boiler(machine);
            var output = machine.container().orElseThrow().getPort(2, ContainerAccess.INTERNAL).asFluid();
            if (boiler.heat() != 300d || boiler.isWorking(0d) ||
                input.getStorageAmount(new FluidStack(Fluids.WATER, 1)) != 1 ||
                !output.getAllStorages().isEmpty() || !boiler.getAllInfo().isEmpty()) {
                helper.fail("Idle boiler changed state without fuel: heat=" + boiler.heat() + ", input=" +
                    input.getStorageAmount(new FluidStack(Fluids.WATER, 1)) + ", output=" + output.getAllStorages(),
                    BOILER_POS);
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(timeoutTicks = 1_300)
    public static void testBoilerHeatsAndProcessesWater(GameTestHelper helper) {
        var machine = placeBoiler(helper);
        var container = machine.container().orElseThrow();
        var fuel = container.getPort(0, ContainerAccess.INTERNAL).asItem();
        var input = container.getPort(1, ContainerAccess.INTERNAL).asFluid();
        var output = container.getPort(2, ContainerAccess.INTERNAL).asFluid();
        fuel.insert(new ItemStack(Items.COAL_BLOCK), false);
        input.insert(new FluidStack(Fluids.WATER, 1_000), false);

        helper.runAfterDelay(120, () -> {
            var boiler = boiler(machine);
            if (!boiler.isWorking(0d) || boiler.heat() <= 300d || boiler.progressTicks() <= 0 ||
                boiler.maxProgressTicks() <= 0 || boiler.getInfo(0, 0).isEmpty()) {
                helper.fail("Boiler did not expose active fuel state: heat=" + boiler.heat() + ", progress=" +
                    boiler.progressTicks() + "/" + boiler.maxProgressTicks(), BOILER_POS);
                return;
            }
            helper.runAfterDelay(900, () -> {
                var boiler1 = boiler(machine);
                var water = input.getStorageAmount(new FluidStack(Fluids.WATER, 1));
                var steam = output.getStorageAmount(new FluidStack(STEAM, 1));
                if (water >= 1_000 || steam <= 0 || boiler1.heat() <= 300d || boiler1.getInfo(1, 0).isEmpty() ||
                    boiler1.getInfo(2, 0).isEmpty() || boiler1.getInfo(1, 1).isPresent() ||
                    boiler1.getInfo(3, 0).isPresent() || boiler1.getAllInfo().size() != 2) {
                    helper.fail("Boiler did not complete water reaction: heat=" + boiler1.heat() + ", water=" +
                        water + ", steam=" + steam + ", info=" + boiler1.getAllInfo().size(), BOILER_POS);
                    return;
                }
                helper.succeed();
            });
        });
    }

    @GameTest(timeoutTicks = 1_300)
    public static void testBoilerBlockedOutputDoesNotConsumeInput(GameTestHelper helper) {
        var machine = placeBoiler(helper);
        var container = machine.container().orElseThrow();
        var fuel = container.getPort(0, ContainerAccess.INTERNAL).asItem();
        var input = container.getPort(1, ContainerAccess.INTERNAL).asFluid();
        var output = container.getPort(2, ContainerAccess.INTERNAL).asFluid();
        output.insert(new FluidStack(Fluids.LAVA, 16_000), false);
        fuel.insert(new ItemStack(Items.COAL_BLOCK), false);
        input.insert(new FluidStack(Fluids.WATER, 1), false);

        helper.runAfterDelay(800, () -> {
            var water = input.getStorageAmount(new FluidStack(Fluids.WATER, 1));
            if (water != 1) {
                helper.fail("Blocked boiler output consumed input: water=" + water + ", output=" +
                    output.getAllStorages(), BOILER_POS);
                return;
            }
            output.extract(16_000, false);
            helper.runAfterDelay(300, () -> {
                if (input.getStorageAmount(new FluidStack(Fluids.WATER, 1)) != 0 ||
                    output.getStorageAmount(new FluidStack(STEAM, 1)) != 1) {
                    helper.fail("Boiler did not resume after blocked output opened", BOILER_POS);
                    return;
                }
                helper.succeed();
            });
        });
    }

    private static IMachine placeBoiler(GameTestHelper helper) {
        var block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(
            TinactoryKeys.ID, "machine/boiler/high"));
        helper.setBlock(BOILER_POS, block.defaultBlockState().setValue(MachineBlock.IO_FACING, Direction.EAST));
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var absolutePos = helper.absolutePos(BOILER_POS);
        helper.getLevel().getBlockState(absolutePos).useItemOn(ItemStack.EMPTY, helper.getLevel(), player,
            InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(absolutePos), Direction.NORTH, absolutePos,
                true));
        return MACHINE.get(helper.getBlockEntity(BOILER_POS));
    }

    private static IBoiler boiler(IMachine machine) {
        return (IBoiler) PROCESSOR.get(machine.blockEntity());
    }
}
