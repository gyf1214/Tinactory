package org.shsts.tinactory.content.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.PortConstraint;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MEPatternIngredientDraft {
    private IStackKey key;
    private long amount = 1L;
    @Nullable
    private Integer port;

    public MEPatternIngredientDraft(IStackKey key) {
        this.key = key;
    }

    public static MEPatternIngredientDraft from(CraftAmount amount) {
        var ret = new MEPatternIngredientDraft(amount.key());
        ret.amount = amount.amount();
        return ret;
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
