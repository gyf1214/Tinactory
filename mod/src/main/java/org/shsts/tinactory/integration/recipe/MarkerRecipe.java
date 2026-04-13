package org.shsts.tinactory.integration.recipe;

import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.multiblock.MultiblockInterface;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinycorelib.api.core.ILoc;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MarkerRecipe extends ProcessingRecipe {
    private final ResourceLocation baseTypeId;
    private final RecipeType<?> baseType;
    private final String prefix;
    private final boolean requireMultiblock;
    @Nullable
    private final IProcessingIngredient displayIngredient;
    @Nullable
    private final Texture displayTex;

    public final List<Input> markerOutputs;

    protected MarkerRecipe(Builder builder) {
        super(builder);
        this.baseTypeId = builder.getBaseTypeId();
        this.baseType = builder.getBaseType();
        this.prefix = builder.prefix;
        this.requireMultiblock = builder.requireMultiblock;
        this.displayIngredient = builder.displayIngredient;
        this.displayTex = builder.displayTex != null ? new Texture(builder.displayTex, 16, 16) : null;
        this.markerOutputs = builder.markerOutputs;
    }

    public Optional<IProcessingIngredient> displayIngredient() {
        return Optional.ofNullable(displayIngredient);
    }

    public Optional<Texture> displayTexture() {
        return Optional.ofNullable(displayTex);
    }

    @Override
    public boolean matches(IMachine machine, int parallel) {
        return false;
    }

    @Override
    public boolean canCraft(IMachine machine) {
        return super.canCraft(machine) &&
            (!requireMultiblock || machine instanceof MultiblockInterface);
    }

    public boolean matchesType(IRecipeType<?> type) {
        return baseType == type.get();
    }

    public boolean matchesType(RecipeType<?> type) {
        return baseType == type;
    }

    public boolean matches(ILoc recipe) {
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
        @Nullable
        private IProcessingIngredient displayIngredient = null;
        @Nullable
        private ResourceLocation displayTex = null;
        private final List<Input> markerOutputs = new ArrayList<>();

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

        public Builder display(IProcessingIngredient value) {
            this.displayIngredient = value;
            return this;
        }

        public Builder display(ItemLike item) {
            return display(ProcessingStackHelper.itemIngredient(new ItemStack(item)));
        }

        public Builder display(TagKey<Item> tag) {
            return display(new TagIngredient(tag, 1));
        }

        public Builder display(ResourceLocation tex) {
            displayTex = tex;
            return this;
        }

        public Builder output(int port, IProcessingIngredient ingredient) {
            markerOutputs.add(new Input(port, ingredient));
            return this;
        }

        public RecipeType<?> getBaseType() {
            assert baseType != null;
            var type = Registry.RECIPE_TYPE.get(baseType);
            assert type != null;
            return type;
        }

        protected ResourceLocation getBaseTypeId() {
            assert baseType != null;
            return baseType;
        }

        @Override
        protected void validate() {}

        @Override
        protected MarkerRecipe createObject() {
            return new MarkerRecipe(this);
        }
    }

    public static class Serializer extends ProcessingRecipe.Serializer<MarkerRecipe, Builder> {
        public Serializer(Codec<IProcessingIngredient> ingredientCodec, Codec<IProcessingResult> resultCodec) {
            super(ingredientCodec, resultCodec);
        }

        @Override
        protected Builder buildFromJson(IRecipeType<Builder> type, ResourceLocation loc, JsonObject jo) {
            var builder = super.buildFromJson(type, loc, jo)
                .baseType(new ResourceLocation(GsonHelper.getAsString(jo, "base_type")))
                .prefix(GsonHelper.getAsString(jo, "prefix", ""))
                .requireMultiblock(GsonHelper.getAsBoolean(jo, "require_multiblock", false));
            if (jo.has("display")) {
                if (jo.get("display").isJsonObject()) {
                    var jo1 = GsonHelper.getAsJsonObject(jo, "display");
                    builder.display(CodecHelper.parseJson(ingredientCodec(), jo1));
                } else {
                    var tex = new ResourceLocation(GsonHelper.getAsString(jo, "display"));
                    builder.display(tex);
                }
            }
            Streams.stream(GsonHelper.getAsJsonArray(jo, "marker_outputs"))
                .map(JsonElement::getAsJsonObject)
                .forEach(je -> builder.output(
                    GsonHelper.getAsInt(je, "port"),
                    CodecHelper.parseJson(ingredientCodec(), GsonHelper.getAsJsonObject(je, "result"))));
            return builder;
        }

        @Override
        public void toJson(JsonObject jo, MarkerRecipe recipe) {
            super.toJson(jo, recipe);
            jo.addProperty("base_type", recipe.baseTypeId.toString());
            jo.addProperty("prefix", recipe.prefix);
            jo.addProperty("require_multiblock", recipe.requireMultiblock);
            if (recipe.displayTex != null) {
                jo.addProperty("display", recipe.displayTex.loc().toString());
            } else if (recipe.displayIngredient != null) {
                jo.add("display", CodecHelper.encodeJson(ingredientCodec(), recipe.displayIngredient));
            }
            var markerOutputs = new JsonArray();
            recipe.markerOutputs.stream()
                .map(output -> {
                    var je = new JsonObject();
                    je.addProperty("port", output.port());
                    je.add("result", CodecHelper.encodeJson(ingredientCodec(), output.ingredient()));
                    return je;
                }).forEach(markerOutputs::add);
            jo.add("marker_outputs", markerOutputs);
        }
    }
}
