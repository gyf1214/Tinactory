package org.shsts.tinactory.content.recipe;

import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.shsts.tinactory.content.machine.Workbench;
import org.shsts.tinactory.core.builder.VanillaRecipeBuilder;
import org.shsts.tinactory.core.tool.ToolItem;
import org.shsts.tinycorelib.api.recipe.IRecipe;
import org.shsts.tinycorelib.api.recipe.IVanillaRecipeSerializer;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.minecraft.world.item.crafting.RecipeSerializer.SHAPED_RECIPE;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ToolRecipe implements IRecipe<Workbench> {
    private final ResourceLocation loc;
    public final ShapedRecipe shapedRecipe;
    public final List<Ingredient> toolIngredients;

    public ToolRecipe(ResourceLocation loc, ShapedRecipe shapedRecipe,
        List<Ingredient> toolIngredients) {
        this.loc = loc;
        this.shapedRecipe = shapedRecipe;
        this.toolIngredients = toolIngredients;
    }

    private boolean matchTools(IItemHandler toolStorage) {
        for (var ingredient : toolIngredients) {
            var found = false;
            for (var i = 0; i < toolStorage.getSlots(); i++) {
                var stack = toolStorage.getStackInSlot(i);
                if (ingredient.test(stack)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean matches(Workbench container, Level world) {
        return shapedRecipe.matches(container.getCraftingContainer(), world) &&
            matchTools(container.getToolStorage());
    }

    public ItemStack assemble() {
        return shapedRecipe.getResultItem().copy();
    }

    public List<ItemStack> getRemainingItems(Workbench container) {
        return shapedRecipe.getRemainingItems(container.getCraftingContainer());
    }

    @Override
    public ResourceLocation loc() {
        return loc;
    }

    public void doDamage(IItemHandlerModifiable toolStorage) {
        var damages = new int[toolStorage.getSlots()];
        Arrays.fill(damages, 0);

        for (var ingredient : toolIngredients) {
            var found = -1;
            for (var i = 0; i < toolStorage.getSlots(); i++) {
                var stack = toolStorage.getStackInSlot(i);
                if (ingredient.test(stack)) {
                    found = i;
                    break;
                }
            }
            if (found != -1) {
                damages[found] += 1;
            }
        }

        for (var i = 0; i < toolStorage.getSlots(); i++) {
            var stack = toolStorage.getStackInSlot(i);
            var stack1 = ToolItem.doDamage(stack, damages[i]);
            toolStorage.setStackInSlot(i, stack1);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + loc + "]";
    }

    private static class FinishedShaped extends ShapedRecipeBuilder.Result {
        @SuppressWarnings("ConstantConditions")
        public FinishedShaped(ResourceLocation loc, Item result,
            int count, List<String> patterns, Map<Character, Ingredient> keys) {
            super(loc, result, count, "", patterns, keys, null, null);
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }

    private static class Finished implements FinishedRecipe {
        private final ResourceLocation loc;
        private final IRecipeType<?> type;
        private final FinishedRecipe shaped;
        private final List<Ingredient> tools;

        public Finished(Builder builder) {
            this.loc = builder.loc;
            this.type = builder.getType();
            this.shaped = builder.createShaped();
            this.tools = builder.tools.stream()
                .map(Supplier::get).toList();
        }

        @Override
        public ResourceLocation getId() {
            return loc;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return type.getSerializer();
        }

        @Override
        public void serializeRecipeData(JsonObject jo) {
            shaped.serializeRecipeData(jo);
            var toolTags = new JsonArray();
            tools.stream().map(Ingredient::toJson).forEach(toolTags::add);
            jo.add("tools", toolTags);
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }

    public static class Builder extends VanillaRecipeBuilder<ToolRecipe, Builder> {
        @Nullable
        private Supplier<? extends ItemLike> result = null;
        private int count = 0;
        private final List<String> rows = new ArrayList<>();
        private final Map<Character, Supplier<Ingredient>> keys = new HashMap<>();
        private final List<Supplier<Ingredient>> tools = new ArrayList<>();

        public Builder(IRecipeType<Builder> parent, ResourceLocation loc) {
            super(parent, loc);
        }

        public Builder result(Supplier<? extends ItemLike> result, int count) {
            this.result = result;
            this.count = count;
            return self();
        }

        public Builder result(Item result, int count) {
            this.result = () -> result;
            this.count = count;
            return self();
        }

        public Builder pattern(String row) {
            rows.add(row);
            return self();
        }

        public Builder define(Character key, Supplier<? extends ItemLike> item) {
            keys.put(key, () -> Ingredient.of(item.get()));
            return self();
        }

        public Builder define(Character key, TagKey<Item> tag) {
            keys.put(key, () -> Ingredient.of(tag));
            return self();
        }

        public Builder define(Character key, Item item) {
            keys.put(key, () -> Ingredient.of(item));
            return self();
        }

        public Builder tool(Supplier<Ingredient> ingredient) {
            tools.add(ingredient);
            return self();
        }

        @SafeVarargs
        public final Builder toolTag(TagKey<Item>... toolTags) {
            for (var tag : toolTags) {
                tool(() -> Ingredient.of(tag));
            }
            return self();
        }

        private IRecipeType<?> getType() {
            return parent;
        }

        private FinishedRecipe createShaped() {
            assert result != null;
            var key = keys.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
            return new FinishedShaped(loc, result.get().asItem(), count, rows, key);
        }

        @Override
        protected FinishedRecipe createObject() {
            return new Finished(this);
        }
    }

    private static class Serializer implements IVanillaRecipeSerializer<ToolRecipe> {
        @Override
        public ToolRecipe fromJson(ResourceLocation loc,
            JsonObject jo, ICondition.IContext context) {
            var shaped = SHAPED_RECIPE.fromJson(loc, jo, context);
            var tools = Streams.stream(GsonHelper.getAsJsonArray(jo, "tools"))
                .map(Ingredient::fromJson)
                .toList();
            return new ToolRecipe(loc, shaped, tools);
        }

        @Override
        public ToolRecipe fromNetwork(ResourceLocation loc, FriendlyByteBuf buf) {
            var shaped = SHAPED_RECIPE.fromNetwork(loc, buf);
            assert shaped != null;
            var tools = buf.readCollection(ArrayList::new, Ingredient::fromNetwork);
            return new ToolRecipe(loc, shaped, tools);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ToolRecipe recipe) {
            SHAPED_RECIPE.toNetwork(buf, recipe.shapedRecipe);
            buf.writeCollection(recipe.toolIngredients,
                (buf1, ingredient) -> ingredient.toNetwork(buf1));
        }
    }

    public static final IVanillaRecipeSerializer<ToolRecipe> SERIALIZER = new Serializer();
}
