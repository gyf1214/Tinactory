package org.shsts.tinactory.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.shsts.tinactory.AllItems;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.content.tool.PoweredDrillItem;
import org.shsts.tinactory.core.electric.Voltage;

import java.util.List;

@GameTestHolder(TinactoryKeys.ID)
public final class PoweredToolGameTest {
    private static volatile BlockPos canceledBreakPos;

    static {
        NeoForge.EVENT_BUS.addListener(PoweredToolGameTest::cancelMarkedBreak);
    }

    @GameTest(template = "empty_8x8x8")
    public static void testPoweredToolBreakConsumesNormalCost(GameTestHelper helper) {
        var pos = new BlockPos(3, 2, 3);
        helper.setBlock(pos, Blocks.STONE);
        var tool = drill(Voltage.LV);
        var player = player(helper, tool, tool.normalUseCost(), false, pos);

        if (!player.gameMode.destroyBlock(helper.absolutePos(pos))) {
            helper.fail("Powered drill refused an affordable ordinary break", pos);
            return;
        }
        if (!helper.getBlockState(pos).isAir() || tool.getPower(player.getMainHandItem()) != 0) {
            helper.fail("Ordinary break did not remove the block and consume exactly normalUseCost", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty_8x8x8")
    public static void testUnderchargedToolCannotBreakAndKeepsCharge(GameTestHelper helper) {
        var pos = new BlockPos(3, 2, 3);
        helper.setBlock(pos, Blocks.STONE);
        var tool = drill(Voltage.LV);
        var charge = tool.normalUseCost() - 1;
        var player = player(helper, tool, charge, false, pos);

        if (player.gameMode.destroyBlock(helper.absolutePos(pos)) || !helper.getBlockState(pos).is(Blocks.STONE) ||
            tool.getPower(player.getMainHandItem()) != charge) {
            helper.fail("Undercharged powered tool broke the block or changed its charge", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty_8x8x8")
    public static void testCanceledBreakConsumesNoCharge(GameTestHelper helper) {
        var pos = new BlockPos(3, 2, 3);
        var absolutePos = helper.absolutePos(pos);
        helper.setBlock(pos, Blocks.STONE);
        var tool = drill(Voltage.LV);
        var charge = tool.normalUseCost() * 2;
        var player = player(helper, tool, charge, false, pos);
        canceledBreakPos = absolutePos;
        boolean broken;
        try {
            broken = player.gameMode.destroyBlock(absolutePos);
        } finally {
            canceledBreakPos = null;
        }

        if (broken || !helper.getBlockState(pos).is(Blocks.STONE) ||
            tool.getPower(player.getMainHandItem()) != charge) {
            helper.fail("Canceled break changed the block or consumed charge", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty_8x8x8")
    public static void testLvDrillHasNoAreaAbility(GameTestHelper helper) {
        var pos = new BlockPos(3, 2, 3);
        var neighbor = pos.above();
        helper.setBlock(pos, Blocks.STONE);
        helper.setBlock(neighbor, Blocks.STONE);
        var tool = drill(Voltage.LV);
        var player = player(helper, tool, tool.capacity(), true, pos);
        clickFace(player, helper, pos, Direction.UP);

        if (!player.gameMode.destroyBlock(helper.absolutePos(pos)) || !helper.getBlockState(pos).isAir() ||
            !helper.getBlockState(neighbor).is(Blocks.STONE) ||
            tool.getPower(player.getMainHandItem()) != tool.capacity() - tool.normalUseCost()) {
            helper.fail("LV drill started an area ability instead of ordinary mining", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty_8x8x8")
    public static void testMvDrillMinesFaceAlignedEligiblePlaneOnce(GameTestHelper helper) {
        var pos = new BlockPos(3, 3, 3);
        for (var y = -1; y <= 1; y++) {
            for (var z = -1; z <= 1; z++) {
                var block = y == -1 && z == -1 ? Blocks.COBWEB :
                    y == 1 && z == 0 ? Blocks.DIRT : Blocks.STONE;
                helper.setBlock(pos.offset(0, y, z), block);
            }
        }
        var outside = pos.east();
        helper.setBlock(outside, Blocks.STONE);
        var tool = drill(Voltage.MV);
        var player = player(helper, tool, tool.capacity(), true, pos);
        clickFace(player, helper, pos, Direction.EAST);

        if (!player.gameMode.destroyBlock(helper.absolutePos(pos))) {
            helper.fail("MV drill refused an affordable area break", pos);
            return;
        }
        for (var y = -1; y <= 1; y++) {
            for (var z = -1; z <= 1; z++) {
                var target = pos.offset(0, y, z);
                var expected = y == -1 && z == -1 ? Blocks.COBWEB : Blocks.AIR;
                if (!helper.getBlockState(target).is(expected)) {
                    helper.fail("MV drill used the wrong face plane or target block tags", target);
                    return;
                }
            }
        }
        if (!helper.getBlockState(outside).is(Blocks.STONE) ||
            tool.getPower(player.getMainHandItem()) != tool.capacity() - tool.specialAbilityCost()) {
            helper.fail("MV drill broke outside its plane or charged more than once", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty_8x8x8")
    public static void testMvDrillPaysSpecialCostWithoutExtraTargets(GameTestHelper helper) {
        var pos = new BlockPos(3, 2, 3);
        helper.setBlock(pos, Blocks.STONE);
        var tool = drill(Voltage.MV);
        var player = player(helper, tool, tool.specialAbilityCost(), true, pos);
        clickFace(player, helper, pos, Direction.UP);

        if (!player.gameMode.destroyBlock(helper.absolutePos(pos)) || !helper.getBlockState(pos).isAir() ||
            tool.getPower(player.getMainHandItem()) != 0) {
            helper.fail("Sneaking drill activation without extra targets did not charge once", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty_8x8x8")
    public static void testHvDrillUsesRadiusTwoPlane(GameTestHelper helper) {
        var pos = new BlockPos(4, 3, 3);
        for (var x = -2; x <= 2; x++) {
            for (var y = -2; y <= 2; y++) {
                helper.setBlock(pos.offset(x, y, 0), Blocks.STONE);
            }
        }
        var tool = drill(Voltage.HV);
        var player = player(helper, tool, tool.capacity(), true, pos);
        clickFace(player, helper, pos, Direction.NORTH);

        if (!player.gameMode.destroyBlock(helper.absolutePos(pos))) {
            helper.fail("HV drill refused an affordable area break", pos);
            return;
        }
        for (var x = -2; x <= 2; x++) {
            for (var y = -2; y <= 2; y++) {
                if (!helper.getBlockState(pos.offset(x, y, 0)).isAir()) {
                    helper.fail("HV drill did not mine its radius-two plane", pos.offset(x, y, 0));
                    return;
                }
            }
        }
        if (tool.getPower(player.getMainHandItem()) != tool.capacity() - tool.specialAbilityCost()) {
            helper.fail("HV drill did not charge its special ability exactly once", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty_8x8x8")
    public static void testUnaffordableDrillSpecialFallsBackToNormalMining(GameTestHelper helper) {
        var pos = new BlockPos(3, 2, 3);
        var neighbor = pos.above();
        helper.setBlock(pos, Blocks.STONE);
        helper.setBlock(neighbor, Blocks.STONE);
        var tool = drill(Voltage.MV);
        var charge = tool.normalUseCost() * 2;
        var player = player(helper, tool, charge, true, pos);
        clickFace(player, helper, pos, Direction.UP);

        if (!player.gameMode.destroyBlock(helper.absolutePos(pos)) || !helper.getBlockState(pos).isAir() ||
            !helper.getBlockState(neighbor).is(Blocks.STONE) ||
            tool.getPower(player.getMainHandItem()) != charge - tool.normalUseCost()) {
            helper.fail("Unaffordable drill special action did not fall back to ordinary mining", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty_8x8x8")
    public static void testMissingDrillFaceFallsBackToNormalMining(GameTestHelper helper) {
        var pos = new BlockPos(3, 2, 3);
        var neighbor = pos.above();
        helper.setBlock(pos, Blocks.STONE);
        helper.setBlock(neighbor, Blocks.STONE);
        var tool = drill(Voltage.MV);
        var player = player(helper, tool, tool.capacity(), true, pos);

        if (!player.gameMode.destroyBlock(helper.absolutePos(pos)) || !helper.getBlockState(pos).isAir() ||
            !helper.getBlockState(neighbor).is(Blocks.STONE) ||
            tool.getPower(player.getMainHandItem()) != tool.capacity() - tool.normalUseCost()) {
            helper.fail("Drill without a captured hit face did not use ordinary mining", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty_8x8x8")
    public static void testDrillExtraBlockBelowToolTierDropsNothing(GameTestHelper helper) {
        var pos = new BlockPos(3, 2, 3);
        var obsidian = pos.east();
        helper.setBlock(pos, Blocks.STONE);
        helper.setBlock(obsidian, Blocks.OBSIDIAN);
        var tool = drill(Voltage.MV);
        var player = player(helper, tool, tool.capacity(), true, pos);
        var stack = player.getMainHandItem();
        stack.set(DataComponents.TOOL, new Tool(List.of(
            Tool.Rule.deniesDrops(Tiers.WOOD.getIncorrectBlocksForDrops()),
            Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_PICKAXE, tool.miningSpeed()),
            Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_SHOVEL, tool.miningSpeed())), 1.0F, 0));
        clickFace(player, helper, pos, Direction.UP);

        if (!player.gameMode.destroyBlock(helper.absolutePos(pos)) || !helper.getBlockState(obsidian).isAir()) {
            helper.fail("Drill did not pass an under-tier extra block through vanilla breaking", obsidian);
            return;
        }
        var absoluteObsidian = helper.absolutePos(obsidian);
        var droppedObsidian = helper.getLevel().getEntities(EntityType.ITEM,
                new AABB(absoluteObsidian).inflate(2), Entity::isAlive).stream()
            .map(ItemEntity::getItem)
            .anyMatch(item -> item.is(Blocks.OBSIDIAN.asItem()));
        if (droppedObsidian) {
            helper.fail("Under-tier drill extra block dropped its item", obsidian);
            return;
        }
        helper.succeed();
    }

    private static PoweredDrillItem drill(Voltage voltage) {
        return AllItems.<PoweredDrillItem>componentEntry("drill").get(voltage).get();
    }

    private static ServerPlayer player(GameTestHelper helper, PoweredDrillItem tool, long charge,
        boolean sneaking, BlockPos pos) {
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        player.setGameMode(GameType.SURVIVAL);
        var absolutePos = helper.absolutePos(pos);
        player.setPos(absolutePos.getX() + 0.5, absolutePos.getY() + 0.5, absolutePos.getZ() + 0.5);
        player.setShiftKeyDown(sneaking);
        var stack = new ItemStack(tool);
        tool.setPower(stack, charge);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        return player;
    }

    private static void clickFace(ServerPlayer player, GameTestHelper helper, BlockPos pos, Direction face) {
        NeoForge.EVENT_BUS.post(new LeftClickBlock(player, helper.absolutePos(pos), face, LeftClickBlock.Action.START));
    }

    private static void cancelMarkedBreak(BlockEvent.BreakEvent event) {
        if (event.getPos().equals(canceledBreakPos)) {
            event.setCanceled(true);
        }
    }
}
