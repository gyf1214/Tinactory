package org.shsts.tinactory.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.shsts.tinactory.AllItems;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.api.tool.IPoweredItem;
import org.shsts.tinactory.core.electric.Voltage;

@GameTestHolder(TinactoryKeys.ID)
public final class PoweredWeaponGameTest {
    private static final long HIT_COST = 4096L;

    @GameTest(template = "empty_8x8x8", timeoutTicks = 80)
    public static void testLivingHitDealsConfiguredDamageAndConsumesOneHit(GameTestHelper helper) {
        var saber = nanoSaber(helper);
        if (saber == null) {
            return;
        }
        floor(helper, 1, 5);
        var player = player(helper, saber, HIT_COST, new BlockPos(2, 2, 2));
        var target = livingTarget(helper, new BlockPos(3, 2, 2));
        var stack = player.getMainHandItem();
        helper.runAfterDelay(20, () -> {
            player.setOnGround(true);
            target.setHealth(target.getMaxHealth());
            player.attack(target);
            if (Math.abs(player.getAttributeValue(Attributes.ATTACK_DAMAGE) - 24.0d) > 0.001d ||
                Math.abs(player.getAttributeValue(Attributes.ATTACK_SPEED) - 1.6d) > 0.001d) {
                helper.fail("Nano Saber did not apply 24 damage and 1.6 attack speed");
                return;
            }
            if (Math.abs(target.getHealth() - 16.0f) > 0.001f || power(saber, stack) != 0L) {
                helper.fail("Accepted hit result was health=" + target.getHealth() + ", damage=" +
                    (target.getMaxHealth() - target.getHealth()) + ", charge=" + power(saber, stack) +
                    ", cooldown=" + player.getAttackStrengthScale(0.5f));
                return;
            }
            if (stack.has(DataComponents.MAX_DAMAGE) || !saber.canPerformAction(stack, ItemAbilities.SWORD_SWEEP)) {
                helper.fail("Nano Saber has durability or does not support the vanilla sword sweep action");
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty_8x8x8", timeoutTicks = 80)
    public static void testUnderchargedAttackIsRejectedWithoutDamageOrChargeChange(GameTestHelper helper) {
        var saber = nanoSaber(helper);
        if (saber == null) {
            return;
        }
        floor(helper, 1, 5);
        var player = player(helper, saber, HIT_COST - 1L, new BlockPos(2, 2, 2));
        var target = livingTarget(helper, new BlockPos(3, 2, 2));
        helper.runAfterDelay(20, () -> {
            player.setOnGround(true);
            target.setHealth(target.getMaxHealth());
            player.attack(target);
            if (target.getHealth() != target.getMaxHealth() ||
                power(saber, player.getMainHandItem()) != HIT_COST - 1L) {
                helper.fail("Undercharged Nano Saber attack damaged the target or changed stored EU");
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty_8x8x8", timeoutTicks = 80)
    public static void testRejectedLivingHitDoesNotConsumeCharge(GameTestHelper helper) {
        var saber = nanoSaber(helper);
        if (saber == null) {
            return;
        }
        floor(helper, 1, 5);
        var player = player(helper, saber, HIT_COST, new BlockPos(2, 2, 2));
        var target = livingTarget(helper, new BlockPos(3, 2, 2));
        target.setInvulnerable(true);
        helper.runAfterDelay(20, () -> {
            player.setOnGround(true);
            target.setHealth(target.getMaxHealth());
            player.attack(target);
            if (target.getHealth() != target.getMaxHealth() || power(saber, player.getMainHandItem()) != HIT_COST) {
                helper.fail("Target-rejected Nano Saber hit damaged the target or consumed EU");
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty_8x8x8", timeoutTicks = 80)
    public static void testSweepHitConsumesOneHitCost(GameTestHelper helper) {
        var saber = nanoSaber(helper);
        if (saber == null) {
            return;
        }
        floor(helper, 1, 5);
        var player = player(helper, saber, HIT_COST * 2L, new BlockPos(2, 2, 2));
        var primary = livingTarget(helper, new BlockPos(3, 2, 2));
        var secondary = livingTarget(helper, new BlockPos(3, 2, 3));
        helper.runAfterDelay(20, () -> {
            player.setOnGround(true);
            primary.setHealth(primary.getMaxHealth());
            secondary.setHealth(secondary.getMaxHealth());
            player.attack(primary);
            if (primary.getHealth() >= primary.getMaxHealth() || secondary.getHealth() >= secondary.getMaxHealth() ||
                power(saber, player.getMainHandItem()) != HIT_COST) {
                helper.fail("Sword sweep did not hit both living targets for one hit cost");
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty_8x8x8", timeoutTicks = 80)
    public static void testNonLivingAttackDoesNotConsumeCharge(GameTestHelper helper) {
        var saber = nanoSaber(helper);
        if (saber == null) {
            return;
        }
        floor(helper, 1, 5);
        var player = player(helper, saber, HIT_COST, new BlockPos(2, 2, 2));
        var target = EntityType.MINECART.create(helper.getLevel());
        if (target == null) {
            helper.fail("Could not create a minecart target");
            return;
        }
        place(helper, target, new BlockPos(3, 2, 2));
        helper.runAfterDelay(20, () -> {
            player.attack(target);
            if (power(saber, player.getMainHandItem()) != HIT_COST) {
                helper.fail("Attack against a non-living target consumed Nano Saber charge");
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty_16x8x8", timeoutTicks = 100)
    public static void testAttackPacingMatchesConfiguredAttackSpeed(GameTestHelper helper) {
        var saber = nanoSaber(helper);
        if (saber == null) {
            return;
        }
        floor(helper, 1, 14);
        var player = player(helper, saber, HIT_COST * 3L, new BlockPos(2, 2, 2));
        var first = livingTarget(helper, new BlockPos(4, 2, 2));
        var early = livingTarget(helper, new BlockPos(8, 2, 2));
        var cooled = livingTarget(helper, new BlockPos(12, 2, 2));
        helper.runAfterDelay(20, () -> {
            player.setOnGround(true);
            first.setHealth(first.getMaxHealth());
            player.attack(first);
            var firstDamage = first.getMaxHealth() - first.getHealth();
            helper.runAfterDelay(6, () -> {
                player.setOnGround(true);
                early.setHealth(early.getMaxHealth());
                player.attack(early);
                var earlyDamage = early.getMaxHealth() - early.getHealth();
                helper.runAfterDelay(13, () -> {
                    player.setOnGround(true);
                    cooled.setHealth(cooled.getMaxHealth());
                    player.attack(cooled);
                    var cooledDamage = cooled.getMaxHealth() - cooled.getHealth();
                    if (firstDamage < 23.9f || earlyDamage <= 0f || earlyDamage >= 23.9f ||
                        cooledDamage < 23.9f || power(saber, player.getMainHandItem()) != 0L) {
                        helper.fail("Nano Saber damage did not recover over the 13-tick 1.6-speed cooldown");
                        return;
                    }
                    helper.succeed();
                });
            });
        });
    }

    private static Item nanoSaber(GameTestHelper helper) {
        var components = AllItems.getComponent("nano_saber");
        if (components == null || !components.containsKey(Voltage.HV)) {
            helper.fail("HV Nano Saber is not registered");
            return null;
        }
        return components.get(Voltage.HV).get().asItem();
    }

    private static Player player(GameTestHelper helper, Item saber, long charge, BlockPos pos) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var absolutePos = helper.absolutePos(pos);
        player.setPos(absolutePos.getX() + 0.5d, absolutePos.getY(), absolutePos.getZ() + 0.5d);
        var stack = new ItemStack(saber);
        ((IPoweredItem) saber).setPower(stack, charge);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.setOnGround(true);
        helper.getLevel().addFreshEntity(player);
        return player;
    }

    private static Cow livingTarget(GameTestHelper helper, BlockPos pos) {
        var target = EntityType.COW.create(helper.getLevel());
        if (target == null) {
            throw new IllegalStateException("Could not create a living test target");
        }
        target.setNoAi(true);
        target.setSilent(true);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40.0d);
        target.setHealth(40.0f);
        place(helper, target, pos);
        return target;
    }

    private static void place(GameTestHelper helper, Entity entity, BlockPos pos) {
        var absolutePos = helper.absolutePos(pos);
        entity.setPos(absolutePos.getX() + 0.5d, absolutePos.getY(), absolutePos.getZ() + 0.5d);
        helper.getLevel().addFreshEntity(entity);
    }

    private static long power(Item saber, ItemStack stack) {
        return ((IPoweredItem) saber).getPower(stack);
    }

    private static void floor(GameTestHelper helper, int minX, int maxX) {
        for (var x = minX; x <= maxX; x++) {
            for (var z = 1; z <= 4; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
        }
    }
}
