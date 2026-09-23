package org.shsts.tinactory.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.shsts.tinactory.AllMaterials;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.api.logistics.ContainerAccess;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineProcessor;
import org.shsts.tinactory.integration.network.MachineBlock;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllCapabilities.PROCESSOR;

@GameTestHolder(TinactoryKeys.ID)
public final class PrimitiveMachineGameTest {
    private static final BlockPos MACHINE_POS = new BlockPos(2, 2, 2);
    private static final Item CHALCOPYRITE = AllMaterials.getMaterial("chalcopyrite").item("raw");
    private static final Item PYRITE = AllMaterials.getMaterial("pyrite").item("raw");

    @GameTest(timeoutTicks = 40)
    public static void testPrimitiveOreAnalyzerProcessesWithoutNetwork(GameTestHelper helper) {
        var block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(
            TinactoryKeys.ID, "primitive/ore_analyzer"));
        helper.setBlock(MACHINE_POS, block.defaultBlockState());
        var machine = MACHINE.get(helper.getBlockEntity(MACHINE_POS));
        var processor = processor(machine);
        var input = machine.container().orElseThrow().getPort(0, ContainerAccess.INTERNAL).asItem();
        var output = machine.container().orElseThrow().getPort(1, ContainerAccess.INTERNAL).asItem();
        if (machine.network().isPresent() || !input.insert(new ItemStack(Items.COBBLESTONE), false).isEmpty() ||
            helper.getBlockState(MACHINE_POS).getValue(MachineBlock.WORKING)) {
            helper.fail("Primitive Ore Analyzer did not start in its standalone idle state", MACHINE_POS);
            return;
        }

        helper.runAfterDelay(1, () -> {
            if (!helper.getBlockState(MACHINE_POS).getValue(MachineBlock.WORKING) ||
                !processor.isWorking(0.25d) || processor.maxProgressTicks() != 1 ||
                processor.workSpeed() <= 0d || processor.workSpeed() >= 1d) {
                helper.fail("Primitive Ore Analyzer did not expose fractional progress: progress=" +
                    processor.getProgress() + ", working=" + processor.isWorking(0.25d) + ", ticks=" +
                    processor.progressTicks() + "/" +
                    processor.maxProgressTicks() + ", speed=" +
                    processor.workSpeed(), MACHINE_POS);
                return;
            }
            helper.runAfterDelay(6, () -> {
                var result = amount(output, CHALCOPYRITE) + amount(output, PYRITE);
                if (amount(input, Items.COBBLESTONE) != 0 || result != 1) {
                    helper.fail("Primitive Ore Analyzer did not produce one valid result: input=" +
                        amount(input, Items.COBBLESTONE) + ", output=" + result, MACHINE_POS);
                    return;
                }
                helper.runAfterDelay(2, () -> {
                    if (helper.getBlockState(MACHINE_POS).getValue(MachineBlock.WORKING)) {
                        helper.fail("Primitive Ore Analyzer stayed in the working state after completion", MACHINE_POS);
                        return;
                    }
                    helper.succeed();
                });
            });
        });
    }

    private static IMachineProcessor processor(IMachine machine) {
        return (IMachineProcessor) PROCESSOR.get(machine.blockEntity());
    }

    private static long amount(IPort<ItemStack> port, Item item) {
        return port.getStorageAmount(new ItemStack(item));
    }
}
