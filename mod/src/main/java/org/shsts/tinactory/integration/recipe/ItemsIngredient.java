package org.shsts.tinactory.integration.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.crafting.Ingredient;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.integration.logistics.ItemPortAdapter;

import java.util.Optional;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ItemsIngredient implements IProcessingIngredient {
    public final Ingredient ingredient;
    public final int amount;

    protected ItemsIngredient(Ingredient ingredient, int amount) {
        this.amount = amount;
        this.ingredient = ingredient;
    }

    @Override
    public PortType type() {
        return PortType.ITEM;
    }

    @Override
    public Predicate<?> filter() {
        return ingredient;
    }

    @Override
    public Optional<IProcessingIngredient> consumePort(IPort<?> port, int parallel, boolean simulate) {
        if (port.type() != PortType.ITEM) {
            return Optional.empty();
        }
        var item = port.asItem();
        if (amount <= 0) {
            return ProcessingStackHelper.findMatchingPort(item, ingredient, ItemPortAdapter.INSTANCE)
                .map(ProcessingStackHelper::itemIngredient);
        } else {
            return ProcessingStackHelper.consumeMatchingPort(item, ingredient, ItemPortAdapter.INSTANCE,
                amount * parallel, simulate).map(ProcessingStackHelper::itemIngredient);
        }
    }

    /**
     * Note that this is not serializable, only for display or JEI purpose.
     */
    public static ItemsIngredient of(Ingredient ingredient, int amount) {
        return new ItemsIngredient(ingredient, amount) {
            @Override
            public String codecName() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
