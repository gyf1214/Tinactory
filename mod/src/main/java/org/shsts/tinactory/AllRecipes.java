package org.shsts.tinactory;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.recipe.BoilerRecipe;
import org.shsts.tinactory.content.recipe.RecipeTypeInfo;
import org.shsts.tinactory.content.recipe.ToolRecipe;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.recipe.MarkerRecipe;
import org.shsts.tinactory.integration.recipe.ProcessingHelper;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance.hasItems;
import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllRecipes {
    public static final IRecipeType<ToolRecipe> TOOL_CRAFTING;
    public static final IRecipeType<BoilerRecipe> BOILER;
    // Recipes only used to mark input for recipe book purpose
    public static final IRecipeType<MarkerRecipe> MARKER;

    public static final Map<String, RecipeTypeInfo> PROCESSING_TYPES = new HashMap<>();

    static {
        TOOL_CRAFTING = REGISTRATE.recipeType("tool_crafting", ToolRecipe.class)
            .serializer(ToolRecipe.CODEC)
            .register();

        BOILER = REGISTRATE.recipeType("boiler", BoilerRecipe.class)
            .serializer(BoilerRecipe.CODEC)
            .register();

        MARKER = REGISTRATE.recipeType("marker", MarkerRecipe.class)
            .serializer(ProcessingHelper.MARKER_CODEC)
            .register();
    }

    public static Criterion<InventoryChangeTrigger.TriggerInstance> hasTag(TagKey<Item> tag) {
        return hasItems(ItemPredicate.Builder.item().of(tag).build());
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
