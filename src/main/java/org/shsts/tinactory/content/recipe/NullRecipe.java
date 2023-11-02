package org.shsts.tinactory.content.recipe;

import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.shsts.tinactory.core.SmartRecipe;
import org.shsts.tinactory.core.SmartRecipeSerializer;
import org.shsts.tinactory.registrate.RecipeTypeEntry;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.builder.SmartRecipeBuilder;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NullRecipe extends SmartRecipe<CraftingContainer, NullRecipe> implements CraftingRecipe {
    public NullRecipe(RecipeTypeEntry<NullRecipe, Builder> type, ResourceLocation loc) {
        super(type, loc);
    }

    @Override
    public boolean matches(CraftingContainer container, Level world) {
        return false;
    }

    @Override
    public ItemStack assemble(CraftingContainer container) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    public static class Builder extends SmartRecipeBuilder<NullRecipe, Builder> {
        public Builder(Registrate registrate, RecipeTypeEntry<NullRecipe, Builder> parent, ResourceLocation loc) {
            super(registrate, parent, loc);
        }

        @Override
        public NullRecipe createObject() {
            return new NullRecipe(this.parent, this.loc);
        }
    }

    private static class Serializer extends SmartRecipeSerializer<NullRecipe, Builder> {
        protected Serializer(RecipeTypeEntry<NullRecipe, Builder> type) {
            super(type);
        }

        @Override
        public NullRecipe fromJson(ResourceLocation loc, JsonObject jo, ICondition.IContext context) {
            return new NullRecipe(this.type, loc);
        }

        @Override
        public void toJson(JsonObject jo, NullRecipe recipe) {}

        @Override
        public NullRecipe fromNetwork(ResourceLocation loc, FriendlyByteBuf buf) {
            return new NullRecipe(this.type, loc);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, NullRecipe recipe) {}
    }

    public static final SmartRecipeSerializer.SimpleFactory<NullRecipe, Builder>
            SERIALIZER = Serializer::new;
}
