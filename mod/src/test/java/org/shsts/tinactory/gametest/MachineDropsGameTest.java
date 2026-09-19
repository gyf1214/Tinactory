package org.shsts.tinactory.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.shsts.tinactory.AllBlockEntities;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;

import java.util.Objects;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllNetworks.MACHINE_NAME;

@GameTestHolder(TinactoryKeys.ID)
public final class MachineDropsGameTest {
    @GameTest
    public static void testMachineDropKeepsCustomName(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        var state = machineState();
        var name = Component.literal("Named Furnace");
        helper.setBlock(pos, state);
        MACHINE.get(helper.getBlockEntity(pos)).setConfig(SetMachineConfigPacket.builder()
            .set(MACHINE_NAME, name).get());

        helper.getLevel().destroyBlock(helper.absolutePos(pos), true, null, 512);

        var drop = helper.getLevel().getEntities(EntityType.ITEM,
                new AABB(helper.absolutePos(pos)).inflate(2), Entity::isAlive).stream()
            .map(ItemEntity::getItem)
            .filter(stack -> stack.is(state.getBlock().asItem()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Named machine did not drop its block item"));
        if (!name.equals(drop.get(DataComponents.CUSTOM_NAME))) {
            helper.fail("Machine drop did not keep its custom name", pos);
            return;
        }
        helper.succeed();
    }

    private static BlockState machineState() {
        return Objects.requireNonNull(AllBlockEntities.getMachine("electric_furnace"), "electric_furnace")
            .block(Voltage.LV)
            .defaultBlockState();
    }
}
