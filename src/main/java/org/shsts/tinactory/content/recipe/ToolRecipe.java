package org.shsts.tinactory.content.recipe;

import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
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
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.shsts.tinactory.content.machine.WorkbenchContainer;
import org.shsts.tinactory.content.tool.ToolItem;
import org.shsts.tinactory.core.SmartRecipe;
import org.shsts.tinactory.core.SmartRecipeSerializer;
import org.shsts.tinactory.registrate.RecipeTypeEntry;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.builder.SimpleRecipeBuilder;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ToolRecipe extends SmartRecipe<WorkbenchContainer, ToolRecipe> {
    private final ShapedRecipe shapedRecipe;
    private final int damage;
    private final List<Ingredient> toolIngredients;

    public ToolRecipe(RecipeTypeEntry<ToolRecipe, Builder> type, ResourceLocation loc, ShapedRecipe shapedRecipe,
                      int damage, List<Ingredient> toolIngredients) {
        super(type, loc);
        this.shapedRecipe = shapedRecipe;
        this.damage = damage;
        this.toolIngredients = toolIngredients;
    }

    protected boolean matchTools(IItemHandler toolStorage) {
        for (var ingredient : this.toolIngredients) {
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
    public boolean matches(WorkbenchContainer container, Level world) {
        return this.shapedRecipe.matches(container.getCraftingContainer(), world) &&
                this.matchTools(container.getToolStorage());
    }

    public void doDamage(IItemHandlerModifiable toolStorage) {
        var damages = new int[toolStorage.getSlots()];
        Arrays.fill(damages, 0);

        for (var ingredient : this.toolIngredients) {
            var found = -1;
            for (var i = 0; i < toolStorage.getSlots(); i++) {
                var stack = toolStorage.getStackInSlot(i);
                if (ingredient.test(stack)) {
                    found = i;
                    break;
                }
            }
            if (found != -1) {
                damages[found] += this.damage;
            }
        }

        for (var i = 0; i < toolStorage.getSlots(); i++) {
            var stack = toolStorage.getStackInSlot(i);
            if (stack.getItem() instanceof ToolItem item) {
                var stack1 = item.doDamage(stack, damages[i]);
                toolStorage.setStackInSlot(i, stack1);
            }
        }
    }

    @Override
    public ItemStack assemble(WorkbenchContainer container) {
        return this.shapedRecipe.assemble(container.getCraftingContainer());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return this.shapedRecipe.canCraftInDimensions(width, height);
    }

    @Override
    public ItemStack getResultItem() {
        return this.shapedRecipe.getResultItem();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(WorkbenchContainer container) {
        return this.shapedRecipe.getRemainingItems(container.getCraftingContainer());
    }

    private static class FinishedShaped extends ShapedRecipeBuilder.Result {
        @SuppressWarnings("ConstantConditions")
        public FinishedShaped(ResourceLocation loc, Item result, int count, List<String> patterns,
                              Map<Character, Ingredient> keys) {
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

    private static class Finished extends SimpleFinished<ToolRecipe> {
        private final FinishedRecipe shaped;
        private final int damage;
        private final List<Ingredient> tools;

        public Finished(ResourceLocation loc, RecipeTypeEntry<ToolRecipe, Builder> type,
                        FinishedRecipe shaped, int damage, List<Ingredient> tools) {
            super(loc, type.getSerializer());
            this.shaped = shaped;
            this.damage = damage;
            this.tools = tools;
        }

        @Override
        public void serializeRecipeData(JsonObject jo) {
            this.shaped.serializeRecipeData(jo);
            jo.addProperty("damage", this.damage);
            var tools = new JsonArray();
            this.tools.stream().map(Ingredient::toJson).forEach(tools::add);
            jo.add("tools", tools);
        }
    }

    public static class Builder extends SimpleRecipeBuilder<RecipeTypeEntry<ToolRecipe, Builder>, Builder> {
        @Nullable
        private Supplier<Item> result = null;
        private int count = 0;
        private final List<String> rows = new ArrayList<>();
        private final Map<Character, Supplier<Ingredient>> key = new HashMap<>();
        private int damage = 0;
        private final List<Supplier<Ingredient>> tools = new ArrayList<>();

        public Builder(Registrate registrate, RecipeTypeEntry<ToolRecipe, Builder> parent, ResourceLocation loc) {
            super(registrate, parent, loc);
        }

        public Builder result(Supplier<Item> result, int count) {
            this.result = result;
            this.count = count;
            return self();
        }

        public Builder pattern(String row) {
            this.rows.add(row);
            return self();
        }

        public Builder define(Character key, TagKey<Item> tag) {
            this.key.put(key, () -> Ingredient.of(tag));
            return self();
        }

        public Builder damage(int damage) {
            this.damage = damage;
            return self();
        }

        public Builder tool(Supplier<Ingredient> ingredient) {
            this.tools.add(ingredient);
            return self();
        }

        public Builder toolTag(ResourceLocation toolTag) {
            return this.tool(() -> Ingredient.of(TagKey.create(Registry.ITEM_REGISTRY, toolTag)));
        }

        @Override
        public FinishedRecipe createObject() {
            assert this.result != null;
            var key = this.key.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
            var tools = this.tools.stream().map(Supplier::get).toList();
            var shaped = new FinishedShaped(this.loc, this.result.get(), this.count, this.rows, key);
            return new Finished(this.loc, this.parent, shaped, this.damage, tools);
        }
    }

    private static class Serializer extends SmartRecipeSerializer<ToolRecipe, Builder> {
        private static final RecipeSerializer<ShapedRecipe> SHAPED_SERIALIZER = RecipeSerializer.SHAPED_RECIPE;

        protected Serializer(RecipeTypeEntry<ToolRecipe, Builder> type) {
            super(type);
        }

        @Override
        public void toJson(JsonObject jo, ToolRecipe recipe) {}

        @Override
        public ToolRecipe fromNetwork(ResourceLocation loc, FriendlyByteBuf buf) {
            var shaped = SHAPED_SERIALIZER.fromNetwork(loc, buf);
            assert shaped != null;
            var damage = buf.readVarInt();
            var tools = buf.readCollection(ArrayList::new, Ingredient::fromNetwork);
            return new ToolRecipe(this.type, loc, shaped, damage, tools);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ToolRecipe recipe) {
            SHAPED_SERIALIZER.toNetwork(buf, recipe.shapedRecipe);
            buf.writeVarInt(recipe.damage);
            buf.writeCollection(recipe.toolIngredients, (buf1, ingredient) -> ingredient.toNetwork(buf1));
        }

        @Override
        public ToolRecipe fromJson(ResourceLocation loc, JsonObject jo, ICondition.IContext context) {
            var shaped = SHAPED_SERIALIZER.fromJson(loc, jo, context);
            var damage = GsonHelper.getAsInt(jo, "damage");
            var tools = Streams.stream(GsonHelper.getAsJsonArray(jo, "tools"))
                    .map(Ingredient::fromJson)
                    .toList();
            return new ToolRecipe(this.type, loc, shaped, damage, tools);
        }
    }

    public static SmartRecipeSerializer<ToolRecipe, Builder> serializer(RecipeTypeEntry<ToolRecipe, Builder> type) {
        return new Serializer(type);
    }
}
