package org.shsts.tinactory.content.recipe;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.multiblock.MultiblockInterface;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeSerializer;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MarkerRecipe extends ProcessingRecipe {
    private final RecipeType<?> baseType;
    private final String prefix;
    private final boolean requireMultiblock;

    private MarkerRecipe(Builder builder) {
        super(builder);
        this.baseType = builder.getBaseType();
        this.prefix = builder.prefix;
        this.requireMultiblock = builder.requireMultiblock;
    }

    @Override
    public Optional<String> getDescriptionId() {
        return Optional.of(getDescriptionId(loc));
    }

    @Override
    public boolean matches(IMachine machine, Level world) {
        return false;
    }

    @Override
    public boolean canCraft(IMachine machine) {
        return super.canCraft(machine) &&
            (!requireMultiblock || machine instanceof MultiblockInterface);
    }

    public boolean matches(IRecipeType<?> type) {
        return baseType == type.get();
    }

    public boolean matches(RecipeType<?> type) {
        return baseType == type;
    }

    public boolean matches(ProcessingRecipe recipe) {
        if (prefix.isEmpty()) {
            return true;
        } else {
            var id = recipe.id();
            return id.equals(prefix) || id.startsWith(prefix + "/");
        }
    }

    public static class Builder extends BuilderBase<MarkerRecipe, Builder> {
        @Nullable
        private ResourceLocation baseType;
        private String prefix = "";
        private boolean requireMultiblock = false;

        public Builder(IRecipeType<Builder> parent, ResourceLocation loc) {
            super(parent, loc);
        }

        public Builder baseType(ResourceLocation value) {
            baseType = value;
            return this;
        }

        public Builder prefix(String value) {
            prefix = value;
            return this;
        }

        public Builder requireMultiblock(boolean value) {
            requireMultiblock = value;
            return this;
        }

        public RecipeType<?> getBaseType() {
            assert baseType != null;
            var type = Registry.RECIPE_TYPE.get(baseType);
            assert type != null;
            return type;
        }

        @Override
        protected void validate() {}

        @Override
        protected MarkerRecipe createObject() {
            return new MarkerRecipe(this);
        }
    }

    private static class Serializer extends ProcessingRecipe.Serializer<MarkerRecipe, Builder> {
        @Override
        protected Builder buildFromJson(IRecipeType<Builder> type, ResourceLocation loc, JsonObject jo) {
            return super.buildFromJson(type, loc, jo)
                .baseType(new ResourceLocation(GsonHelper.getAsString(jo, "base_type")))
                .prefix(GsonHelper.getAsString(jo, "prefix", ""))
                .requireMultiblock(GsonHelper.getAsBoolean(jo, "require_multiblock", false));
        }

        @Override
        public void toJson(JsonObject jo, MarkerRecipe recipe) {
            super.toJson(jo, recipe);
            jo.addProperty("base_type", recipe.baseType.toString());
            jo.addProperty("prefix", recipe.prefix);
            jo.addProperty("require_multiblock", recipe.requireMultiblock);
        }
    }

    public static final IRecipeSerializer<MarkerRecipe, Builder> SERIALIZER = new Serializer();
}
