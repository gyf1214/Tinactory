package org.shsts.tinactory.content.tool;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.List;

import static org.shsts.tinactory.integration.util.ClientUtil.NUMBER_FORMAT;
import static org.shsts.tinactory.integration.util.ClientUtil.addTooltip;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class PoweredWeaponItem extends PoweredItem {
    private final long hitCost;

    public PoweredWeaponItem(Properties properties, PoweredWeaponConfig config) {
        super(properties.attributes(attributes(config)), config.voltage(), config.capacity());
        this.hitCost = config.hitCost();
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return getPower(stack) < hitCost;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide) {
            charge(stack, -hitCost);
        }
        return false;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return itemAbility == ItemAbilities.SWORD_SWEEP;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
        TooltipFlag flag) {
        var hits = Math.floorDiv(getPower(stack), hitCost);
        addTooltip(tooltip, "powered", NUMBER_FORMAT.format(hits));
        super.appendHoverText(stack, context, tooltip, flag);
    }

    private static ItemAttributeModifiers attributes(PoweredWeaponConfig config) {
        return ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, config.attackDamage() - 1.0d,
                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED,
                new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, config.attackSpeed() - 4.0d,
                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .build();
    }
}
