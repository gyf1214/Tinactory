package org.shsts.tinactory.core.common;

import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.core.logistics.NullContainer;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SmartRecipe<C, T extends SmartRecipe<C, T>>
        implements Recipe<SmartRecipe.ContainerWrapper<C>>, ISelf<T> {

    @FunctionalInterface
    public interface Factory<T extends SmartRecipe<?, T>> {
        T create(RecipeTypeEntry<T, ?> type, ResourceLocation loc);
    }

    protected final ResourceLocation loc;
    protected final RecipeType<? super T> type;
    protected final RecipeSerializer<T> serializer;

    protected SmartRecipe(RecipeTypeEntry<T, ?> type, ResourceLocation loc) {
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
        return this.type;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return this.serializer;
    }

    @Override
    public ResourceLocation getId() {
        return this.loc;
    }

    @Override
    public boolean matches(ContainerWrapper<C> wrapper, Level world) {
        return this.matches(wrapper.compose, world);
    }

    public abstract boolean matches(C container, Level world);

    @Override
    public ItemStack assemble(ContainerWrapper<C> wrapper) {
        return this.assemble(wrapper.compose);
    }

    public abstract ItemStack assemble(C container);

    @Override
    public abstract boolean canCraftInDimensions(int width, int height);

    @Override
    public NonNullList<ItemStack> getRemainingItems(ContainerWrapper<C> container) {
        return this.getRemainingItems(container.compose);
    }

    public NonNullList<ItemStack> getRemainingItems(C container) {
        return NonNullList.create();
    }

    public static <C1, T1 extends SmartRecipe<C1, ?>> Optional<T1>
    getRecipeFor(RecipeType<T1> type, C1 container, Level world) {
        return world.getRecipeManager().getRecipeFor(type, new ContainerWrapper<>(container), world);
    }

    protected abstract static class SimpleFinished<T extends Recipe<?>> implements FinishedRecipe {
        protected final ResourceLocation loc;
        protected final RecipeSerializer<T> serializer;

        public SimpleFinished(ResourceLocation loc, RecipeSerializer<T> serializer) {
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
        return new SimpleFinished<>(this.loc, this.serializer) {
            @Override
            public void serializeRecipeData(JsonObject jo) {
                ((SmartRecipeSerializer<T, ?>) serializer).toJson(jo, SmartRecipe.this.self());
            }
        };
    }
}
