package org.shsts.tinactory.integration.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingDisplay;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.core.gui.EmptyRenderDescriptor;
import org.shsts.tinactory.core.gui.IRenderDescriptor;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinactory.integration.gui.client.ItemRenderDescriptor;
import org.shsts.tinactory.integration.logistics.StackHelper;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ItemsIngredient implements IProcessingIngredient, IProcessingDisplay {
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
            return ProcessingHelper.findMatchingPort(item, ingredient, StackHelper.ITEM_ADAPTER)
                .map(ProcessingHelper::itemIngredient);
        } else {
            return ProcessingHelper.consumeMatchingPort(item, ingredient, StackHelper.ITEM_ADAPTER,
                amount * parallel, simulate).map(ProcessingHelper::itemIngredient);
        }
    }

    @Override
    public IRenderDescriptor display() {
        return ClientUtil.selectItemFromItems(ingredient)
            .<IRenderDescriptor>map(ItemRenderDescriptor::new)
            .orElse(EmptyRenderDescriptor.INSTANCE);
    }

    @Override
    public Optional<List<Component>> tooltip() {
        return ClientUtil.selectItemFromItems(ingredient).map(ClientUtil::itemTooltip);
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
