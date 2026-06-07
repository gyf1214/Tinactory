package org.shsts.tinactory.content.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.logistics.IStackAdapter;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.PortConstraint;
import org.shsts.tinactory.integration.logistics.StackHelper;

import java.util.Optional;

import static org.shsts.tinactory.integration.logistics.StackHelper.FLUID_ADAPTER;
import static org.shsts.tinactory.integration.logistics.StackHelper.ITEM_ADAPTER;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MEPatternIngredientDraft {
    private IStackKey key;
    private long amount;
    @Nullable
    private Integer port;

    public MEPatternIngredientDraft(IStackKey key, long amount) {
        this(key, amount, null);
    }

    private MEPatternIngredientDraft(IStackKey key, long amount, @Nullable Integer port) {
        this.key = key;
        this.amount = Math.max(1L, amount);
        this.port = port;
    }

    public static MEPatternIngredientDraft from(CraftAmount amount) {
        return new MEPatternIngredientDraft(amount.key(), amount.amount());
    }

    public static <T> MEPatternIngredientDraft from(IStackAdapter<T> adapter, T stack) {
        return new MEPatternIngredientDraft(adapter.keyOf(stack), adapter.amount(stack));
    }

    public static Optional<MEPatternIngredientDraft> fromItem(ItemStack stack) {
        var fluid = StackHelper.getFluidFromItem(stack);
        return Optional.of(fluid)
            .filter($ -> !$.isEmpty())
            .map($ -> from(FLUID_ADAPTER, $))
            .or(() -> stack.isEmpty() ? Optional.empty() : Optional.of(from(ITEM_ADAPTER, stack)));
    }

    public MEPatternIngredientDraft copy() {
        return new MEPatternIngredientDraft(key, amount, port);
    }

    public IStackKey key() {
        return key;
    }

    public void setKey(@Nullable IStackKey value) {
        key = value;
    }

    public long amount() {
        return amount;
    }

    public void setAmount(long value) {
        amount = Math.max(1L, value);
    }

    @Nullable
    public Integer port() {
        return port;
    }

    public void setPort(@Nullable Integer value) {
        port = value == null || value < 0 ? null : value;
    }

    public boolean isEmpty() {
        return key == null;
    }

    public CraftAmount toAmount() {
        if (key == null) {
            throw new IllegalStateException("Cannot convert empty pattern ingredient");
        }
        return new CraftAmount(key, amount);
    }

    public PortConstraint toConstraint(PortDirection direction, int index) {
        if (port == null) {
            throw new IllegalStateException("Cannot convert ingredient without port constraint");
        }
        return new PortConstraint(direction, index, port);
    }
}
