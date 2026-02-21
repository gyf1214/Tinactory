package org.shsts.tinactory;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.recipe.BoilerRecipe;
import org.shsts.tinactory.content.recipe.RecipeTypeInfo;
import org.shsts.tinactory.content.recipe.ToolRecipe;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.recipe.MarkerRecipe;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.HashMap;
import java.util.Map;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllRecipes {
    public static final IRecipeType<ToolRecipe.Builder> TOOL_CRAFTING;
    public static final IRecipeType<BoilerRecipe.Builder> BOILER;
    // Recipes only used to mark input for recipe book purpose
    public static final IRecipeType<MarkerRecipe.Builder> MARKER;

    public static final Map<String, RecipeTypeInfo> PROCESSING_TYPES = new HashMap<>();

    static {
        TOOL_CRAFTING = REGISTRATE.vanillaRecipeType("tool_crafting", ToolRecipe.Builder::new)
            .recipeClass(ToolRecipe.class)
            .serializer(ToolRecipe.SERIALIZER)
            .register();

        BOILER = REGISTRATE.recipeType("boiler", BoilerRecipe.Builder::new)
            .recipeClass(BoilerRecipe.class)
            .serializer(BoilerRecipe.SERIALIZER)
            .register();

        MARKER = REGISTRATE.recipeType("marker", MarkerRecipe.Builder::new)
            .recipeClass(MarkerRecipe.class)
            .serializer(MarkerRecipe.SERIALIZER)
            .register();
    }

    public static InventoryChangeTrigger.TriggerInstance has(TagKey<Item> tag) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(tag).build());
    }

    public static InventoryChangeTrigger.TriggerInstance has(ItemLike item) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(item).build());
    }

    private static InventoryChangeTrigger.TriggerInstance inventoryTrigger(ItemPredicate... predicates) {
        return new InventoryChangeTrigger.TriggerInstance(EntityPredicate.Composite.ANY,
            MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, predicates);
    }

    public static void putTypeInfo(IRecipeType<?> recipeType, Layout layout,
        IEntry<? extends Block> icon) {
        var id = recipeType.id();
        if (!PROCESSING_TYPES.containsKey(id)) {
            PROCESSING_TYPES.put(id, new RecipeTypeInfo(recipeType, layout, icon));
        }
    }

    public static RecipeTypeInfo getTypeInfo(String id) {
        return PROCESSING_TYPES.get(id);
    }

    public static void init() {}
}
