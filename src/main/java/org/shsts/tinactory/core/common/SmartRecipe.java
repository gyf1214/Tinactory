package org.shsts.tinactory.core.common;

import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.core.logistics.NullContainer;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SmartRecipe<C> implements Recipe<SmartRecipe.ContainerWrapper<C>> {
    @FunctionalInterface
    public interface Factory<T extends SmartRecipe<?>> {
        T create(RecipeTypeEntry<T, ?> type, ResourceLocation loc);
    }

    private final ResourceLocation loc;
    private final RecipeType<?> type;
    private final SmartRecipeSerializer<?, ?> serializer;

    protected SmartRecipe(RecipeTypeEntry<?, ?> type, ResourceLocation loc) {
        this.loc = loc;
        this.type = type.get();
        this.serializer = type.getSerializer();
    }

    public static class ContainerWrapper<C1> implements NullContainer {
        private final C1 compose;

        public ContainerWrapper(C1 compose) {
            this.compose = compose;
        }
    }

    @Override
    public RecipeType<?> getType() {
        return type;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return serializer;
    }

    @Override
    public ResourceLocation getId() {
        return loc;
    }

    @Override
    public boolean matches(ContainerWrapper<C> wrapper, Level world) {
        return matches(wrapper.compose, world);
    }

    public abstract boolean matches(C container, Level world);

    @Override
    public ItemStack assemble(ContainerWrapper<C> wrapper) {
        return assemble(wrapper.compose);
    }

    public ItemStack assemble(C container) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    /**
     * Whether the recipe is available in the container, regardless of the inputs
     */
    public boolean canCraftIn(C container) {
        return true;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(ContainerWrapper<C> container) {
        return getRemainingItems(container.compose);
    }

    public NonNullList<ItemStack> getRemainingItems(C container) {
        return NonNullList.create();
    }

    public static <C1, T1 extends SmartRecipe<C1>> Optional<T1>
    getRecipeFor(RecipeType<T1> type, C1 container, Level world) {
        return world.getRecipeManager().getRecipeFor(type, new ContainerWrapper<>(container), world);
    }

    public static <C1, T1 extends SmartRecipe<C1>> List<T1>
    getRecipesFor(RecipeType<T1> type, C1 container, Level world) {
        return world.getRecipeManager().getRecipesFor(type, new ContainerWrapper<>(container), world);
    }

    public static String getDescriptionId(ResourceLocation loc) {
        return loc.getNamespace() + ".recipe." + loc.getPath().replace('/', '.');
    }

    public String getDescriptionId() {
        return getDescriptionId(loc);
    }

    public Component getDescription() {
        return I18n.tr(getDescriptionId(loc));
    }

    public boolean hasDescription() {
        return false;
    }

    protected abstract static class SimpleFinished implements FinishedRecipe {
        protected final ResourceLocation loc;
        protected final SmartRecipeSerializer<?, ?> serializer;

        public SimpleFinished(ResourceLocation loc, SmartRecipeSerializer<?, ?> serializer) {
            this.loc = loc;
            this.serializer = serializer;
        }

        public abstract void serializeRecipeData(JsonObject jo);

        @Override
        public ResourceLocation getId() {
            return loc;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return serializer;
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

    public FinishedRecipe toFinished() {
        return new SimpleFinished(loc, serializer) {
            @Override
            public void serializeRecipeData(JsonObject jo) {
                serializer.recipeToJson(jo, SmartRecipe.this);
            }
        };
    }

    @Override
    public String toString() {
        return "%s{%s}".formatted(getClass().getSimpleName(), loc);
    }
}
