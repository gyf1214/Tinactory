package org.shsts.tinactory.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.shsts.tinactory.AllItems;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.content.tool.PoweredChainsawItem;
import org.shsts.tinactory.content.tool.PoweredDrillItem;
import org.shsts.tinactory.content.tool.PoweredToolItem;
import org.shsts.tinactory.core.electric.Voltage;

import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.AllDataComponents.POWERED_ACTIVATED;

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
            helper.fail("Activated drill without extra targets did not charge once", pos);
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

    @GameTest(template = "empty_8x8x8")
    public static void testChainsawReachesLogsThroughLeaves(GameTestHelper helper) {
        var pos = new BlockPos(3, 2, 3);
        var leaves = pos.east();
        var log = leaves.east();
        var secondLeaves = log.east();
        helper.setBlock(pos, Blocks.OAK_LOG);
        helper.setBlock(leaves, Blocks.OAK_LEAVES);
        helper.setBlock(log, Blocks.OAK_LOG);
        helper.setBlock(secondLeaves, Blocks.OAK_LEAVES);
        var tool = chainsaw(Voltage.MV);
        var player = player(helper, tool, tool.specialAbilityCost(), true, pos);

        if (!player.gameMode.destroyBlock(helper.absolutePos(pos)) || !helper.getBlockState(pos).isAir() ||
            !helper.getBlockState(log).isAir() || !helper.getBlockState(leaves).is(Blocks.OAK_LEAVES) ||
            !helper.getBlockState(secondLeaves).is(Blocks.OAK_LEAVES) ||
            tool.getPower(player.getMainHandItem()) != 0) {
            helper.fail("Chainsaw did not fell connected logs through leaves for one special cost", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty_8x8x8")
    public static void testChainsawStopsAtOtherBlocksAndSkipsDisconnectedLogs(GameTestHelper helper) {
        var pos = new BlockPos(3, 2, 3);
        var leaves = pos.east();
        var obstruction = leaves.east();
        var blockedLog = obstruction.east();
        var disconnectedLog = pos.west().west();
        helper.setBlock(pos, Blocks.OAK_LOG);
        helper.setBlock(leaves, Blocks.OAK_LEAVES);
        helper.setBlock(obstruction, Blocks.STONE);
        helper.setBlock(blockedLog, Blocks.OAK_LOG);
        helper.setBlock(disconnectedLog, Blocks.OAK_LOG);
        var tool = chainsaw(Voltage.MV);
        var player = player(helper, tool, tool.specialAbilityCost(), true, pos);

        if (!player.gameMode.destroyBlock(helper.absolutePos(pos)) || !helper.getBlockState(pos).isAir() ||
            !helper.getBlockState(leaves).is(Blocks.OAK_LEAVES) ||
            !helper.getBlockState(obstruction).is(Blocks.STONE) ||
            !helper.getBlockState(blockedLog).is(Blocks.OAK_LOG) ||
            !helper.getBlockState(disconnectedLog).is(Blocks.OAK_LOG) ||
            tool.getPower(player.getMainHandItem()) != 0) {
            helper.fail("Chainsaw crossed a non-tree block or felled a disconnected log", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty_8x8x8")
    public static void testMvChainsawPaysSpecialCostWithoutExtraLogs(GameTestHelper helper) {
        var pos = new BlockPos(3, 2, 3);
        helper.setBlock(pos, Blocks.OAK_LOG);
        var tool = chainsaw(Voltage.MV);
        var player = player(helper, tool, tool.specialAbilityCost(), true, pos);

        if (!player.gameMode.destroyBlock(helper.absolutePos(pos)) || !helper.getBlockState(pos).isAir() ||
            tool.getPower(player.getMainHandItem()) != 0) {
            helper.fail("Activated chainsaw without connected extra logs did not charge once", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty_8x8x8")
    public static void testChainsawChargesOnceAndClearsBreakGuard(GameTestHelper helper) {
        var pos = new BlockPos(3, 2, 3);
        var leaves = pos.east();
        var extraLog = leaves.east();
        var followup = pos.south();
        helper.setBlock(pos, Blocks.OAK_LOG);
        helper.setBlock(leaves, Blocks.OAK_LEAVES);
        helper.setBlock(extraLog, Blocks.OAK_LOG);
        helper.setBlock(followup, Blocks.STONE);
        var tool = chainsaw(Voltage.MV);
        var player = player(helper, tool, tool.specialAbilityCost(), true, pos);

        if (!player.gameMode.destroyBlock(helper.absolutePos(pos)) || !helper.getBlockState(pos).isAir() ||
            !helper.getBlockState(extraLog).isAir() || !helper.getBlockState(leaves).is(Blocks.OAK_LEAVES) ||
            tool.getPower(player.getMainHandItem()) != 0) {
            helper.fail("Chainsaw did not charge one special cost for all connected logs", pos);
            return;
        }
        if (player.gameMode.destroyBlock(helper.absolutePos(followup)) ||
            !helper.getBlockState(followup).is(Blocks.STONE)) {
            helper.fail("Chainsaw break guard remained active after tree felling", followup);
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty_8x8x8")
    public static void testUnaffordableChainsawSpecialFallsBackToNormalMining(GameTestHelper helper) {
        var pos = new BlockPos(3, 2, 3);
        var extraLog = pos.east();
        helper.setBlock(pos, Blocks.OAK_LOG);
        helper.setBlock(extraLog, Blocks.OAK_LOG);
        var tool = chainsaw(Voltage.MV);
        var charge = tool.normalUseCost() * 2;
        var player = player(helper, tool, charge, true, pos);

        if (!player.gameMode.destroyBlock(helper.absolutePos(pos)) || !helper.getBlockState(pos).isAir() ||
            !helper.getBlockState(extraLog).is(Blocks.OAK_LOG) ||
            tool.getPower(player.getMainHandItem()) != charge - tool.normalUseCost()) {
            helper.fail("Unaffordable chainsaw special action did not fall back to ordinary mining", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty_64x16x64")
    public static void testMvChainsawCountsLogsAndLeavesAgainstSearchBudget(GameTestHelper helper) {
        assertChainsawSearchBudget(helper, Voltage.MV, 64);
    }

    @GameTest(template = "empty_64x16x64")
    public static void testHvChainsawCountsLogsAndLeavesAgainstSearchBudget(GameTestHelper helper) {
        assertChainsawSearchBudget(helper, Voltage.HV, 128);
    }

    @GameTest(template = "empty_8x8x8")
    public static void testChainsawStripsLogForNormalCost(GameTestHelper helper) {
        assertChainsawTransformation(helper, Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG, true, "strip");
    }

    @GameTest(template = "empty_8x8x8")
    public static void testChainsawDoesNotChargeWhenStripFails(GameTestHelper helper) {
        assertChainsawTransformation(helper, Blocks.STONE, Blocks.STONE, false, "strip");
    }

    @GameTest(template = "empty_8x8x8")
    public static void testChainsawScrapesCopperForNormalCost(GameTestHelper helper) {
        assertChainsawTransformation(helper, Blocks.OXIDIZED_COPPER, Blocks.WEATHERED_COPPER, true, "scrape");
    }

    @GameTest(template = "empty_8x8x8")
    public static void testChainsawDoesNotChargeWhenScrapeFails(GameTestHelper helper) {
        assertChainsawTransformation(helper, Blocks.STONE, Blocks.STONE, false, "scrape");
    }

    @GameTest(template = "empty_8x8x8")
    public static void testChainsawRemovesWaxForNormalCost(GameTestHelper helper) {
        assertChainsawTransformation(helper, Blocks.WAXED_COPPER_BLOCK, Blocks.COPPER_BLOCK, true, "wax-off");
    }

    @GameTest(template = "empty_8x8x8")
    public static void testChainsawDoesNotChargeWhenWaxOffFails(GameTestHelper helper) {
        assertChainsawTransformation(helper, Blocks.STONE, Blocks.STONE, false, "wax-off");
    }

    private static PoweredDrillItem drill(Voltage voltage) {
        return AllItems.<PoweredDrillItem>componentEntry("drill").get(voltage).get();
    }

    private static PoweredChainsawItem chainsaw(Voltage voltage) {
        return AllItems.<PoweredChainsawItem>componentEntry("chainsaw").get(voltage).get();
    }

    private static void assertChainsawSearchBudget(GameTestHelper helper, Voltage voltage, int searchLimit) {
        var path = chainsawSearchPath(searchLimit + 1);
        var pos = path.getFirst();
        var logCount = searchLimit / 2;
        for (var index = 0; index < searchLimit; index++) {
            helper.setBlock(path.get(index), index < logCount ? Blocks.OAK_LOG : Blocks.OAK_LEAVES);
        }
        var outsideBudget = path.get(searchLimit);
        helper.setBlock(outsideBudget, Blocks.OAK_LOG);
        var tool = chainsaw(voltage);
        var player = player(helper, tool, tool.capacity(), true, pos);

        if (!player.gameMode.destroyBlock(helper.absolutePos(pos))) {
            helper.fail("Chainsaw refused an affordable search-budget break", pos);
            return;
        }
        for (var index = 0; index < logCount; index++) {
            if (!helper.getBlockState(path.get(index)).isAir()) {
                helper.fail("Chainsaw did not fell a log inside its search budget", path.get(index));
                return;
            }
        }
        for (var index = logCount; index < searchLimit; index++) {
            if (!helper.getBlockState(path.get(index)).is(Blocks.OAK_LEAVES)) {
                helper.fail("Chainsaw broke a leaf or stopped counting leaves against its budget", path.get(index));
                return;
            }
        }
        if (!helper.getBlockState(outsideBudget).is(Blocks.OAK_LOG) ||
            tool.getPower(player.getMainHandItem()) != tool.capacity() - tool.specialAbilityCost()) {
            helper.fail("Chainsaw exceeded its search budget or charged more than once", outsideBudget);
            return;
        }
        helper.succeed();
    }

    private static List<BlockPos> chainsawSearchPath(int length) {
        var path = new ArrayList<BlockPos>(length);
        var rowLength = 40;
        for (var row = 0; path.size() < length; row++) {
            var z = 1 + row * 2;
            var x = row % 2 == 0 ? 1 : rowLength;
            var direction = row % 2 == 0 ? 1 : -1;
            while (x >= 1 && x <= rowLength && path.size() < length) {
                path.add(new BlockPos(x, 2, z));
                x += direction;
            }
            if (path.size() < length) {
                path.add(new BlockPos(row % 2 == 0 ? rowLength : 1, 2, z + 1));
            }
        }
        return path;
    }

    private static void assertChainsawTransformation(GameTestHelper helper, Block input, Block expected,
        boolean success, String action) {
        var pos = new BlockPos(3, 2, 3);
        helper.setBlock(pos, input);
        var tool = chainsaw(Voltage.LV);
        var player = player(helper, tool, tool.normalUseCost(), false, pos);
        var absolutePos = helper.absolutePos(pos);
        var context = new UseOnContext(player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(absolutePos), Direction.UP, absolutePos, false));
        var result = player.getMainHandItem().useOn(context);

        if (result.consumesAction() != success || !helper.getBlockState(pos).is(expected) ||
            tool.getPower(player.getMainHandItem()) != (success ? 0 : tool.normalUseCost())) {
            helper.fail("Chainsaw " + action + " transformation had the wrong result or charge cost", pos);
            return;
        }
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper, PoweredToolItem tool, long charge,
        boolean specialAbilityActivated, BlockPos pos) {
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        player.setGameMode(GameType.SURVIVAL);
        var absolutePos = helper.absolutePos(pos);
        player.setPos(absolutePos.getX() + 0.5, absolutePos.getY() + 0.5, absolutePos.getZ() + 0.5);
        var stack = new ItemStack(tool);
        tool.setPower(stack, charge);
        if (specialAbilityActivated) {
            stack.set(POWERED_ACTIVATED, Unit.INSTANCE);
        }
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
