package org.shsts.tinactory.content.multiblock;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.machine.MachineMeta;
import org.shsts.tinactory.content.machine.UnsupportedTypeException;
import org.shsts.tinactory.content.network.PrimitiveBlock;
import org.shsts.tinactory.content.recipe.RecipeTypeInfo;
import org.shsts.tinactory.core.builder.BlockEntityBuilder;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.machine.RecipeProcessors;
import org.shsts.tinactory.core.multiblock.Multiblock;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllMultiblocks.MULTIBLOCK_SETS;
import static org.shsts.tinactory.content.AllRecipes.PROCESSING_TYPES;
import static org.shsts.tinactory.content.AllRegistries.BLOCKS;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockMeta extends MachineMeta {
    public MultiblockMeta() {
        super("Multiblock");
    }

    private static class Executor extends MachineMeta.Executor {
        public Executor(ResourceLocation loc, JsonObject jo) {
            super(loc, jo);
        }

        private <P> Multiblock.Builder<P> multiblock(IBlockEntityTypeBuilder<P> builder) {
            var autoRecipe = GsonHelper.getAsBoolean(jo, "autoRecipe", true);
            switch (recipeTypeStr) {
                case "blast_furnace" -> {
                    return builder.transform(RecipeProcessors.blastFurnace(recipeType()))
                        .child(Multiblock.builder(CoilMultiblock::new));
                }
                case "distillation" -> {
                    return builder.transform(RecipeProcessors.multiblock(recipeType(), autoRecipe))
                        .child(Multiblock.builder(DistillationTower::new));
                }
            }
            switch (machineType) {
                case "default" -> {
                    return builder.child(Multiblock.simple(recipeType(), autoRecipe));
                }
                case "coil" -> {
                    var baseTemp = GsonHelper.getAsInt(jo, "baseTemperature");
                    return builder.transform(RecipeProcessors.coil(recipeType(), autoRecipe, baseTemp))
                        .child(Multiblock.builder(CoilMultiblock::new));
                }
                case "engraving" -> {
                    return builder.transform(RecipeProcessors.multiblock(recipeType(), autoRecipe))
                        .child(Multiblock.builder(Lithography::new));
                }
            }
            throw new UnsupportedTypeException("machine", machineType);
        }

        @Override
        protected IRecipeType<?> getRecipeType() {
            if (recipeTypeStr.contains(":")) {
                return REGISTRATE.getRecipeType(new ResourceLocation(recipeTypeStr));
            }
            return super.getRecipeType();
        }

        private <P> Transformer<MultiblockSpec.Builder<P>> parseLayer(JsonElement je) {
            if (je.isJsonArray()) {
                var ja = je.getAsJsonArray();
                return builder -> {
                    var layer = builder.layer();
                    for (var je1 : ja) {
                        var line = GsonHelper.convertToString(je1, "rows");
                        layer.row(line);
                    }
                    return layer.build();
                };
            } else if (je.isJsonObject()) {
                var jo1 = je.getAsJsonObject();
                var je1 = jo1.get("height");
                int minHeight, maxHeight;
                if (je1 == null) {
                    minHeight = maxHeight = 1;
                } else if (je1.isJsonPrimitive() && je1.getAsJsonPrimitive().isString()) {
                    var s = je1.getAsString().split("-");
                    minHeight = Integer.parseInt(s[0]);
                    maxHeight = Integer.parseInt(s[1]);
                } else if (je1.isJsonPrimitive() && je1.getAsJsonPrimitive().isNumber()) {
                    minHeight = maxHeight = je1.getAsInt();
                } else {
                    throw new JsonSyntaxException("Except field height to be either number or string");
                }
                var ja2 = GsonHelper.getAsJsonArray(jo1, "rows");
                return builder -> {
                    var layer = builder.layer().height(minHeight, maxHeight);
                    for (var je2 : ja2) {
                        var line = GsonHelper.convertToString(je2, "rows");
                        layer.row(line);
                    }
                    return layer.build();
                };
            }
            throw new JsonSyntaxException("Except field layers to be either JsonArray or JsonObject");
        }

        private <P> Transformer<MultiblockSpec.Builder<P>> parseDefine(Character ch, JsonElement je) {
            if (je.isJsonPrimitive() && je.getAsJsonPrimitive().isString()) {
                var s = je.getAsString();
                if (s.equals("air")) {
                    return $ -> $.air(ch);
                } else {
                    return $ -> $.block(ch, BLOCKS.getEntry(new ResourceLocation(s)));
                }
            }

            var jo = GsonHelper.convertToJsonObject(je, "defines");
            var type = GsonHelper.getAsString(jo, "type");
            switch (type) {
                case "block_or_interface" -> {
                    var block = BLOCKS.getEntry(new ResourceLocation(GsonHelper.getAsString(jo, "block")));
                    return $ -> $.blockOrInterface(ch, block);
                }
                case "tag" -> {
                    var tag = AllTags.block(new ResourceLocation(GsonHelper.getAsString(jo, "tag")));
                    return $ -> $.tag(ch, tag);
                }
                case "tag_with_same_block" -> {
                    var tag = AllTags.block(new ResourceLocation(GsonHelper.getAsString(jo, "tag")));
                    var key = GsonHelper.getAsString(jo, "key");
                    return $ -> $.tagWithSameBlock(ch, key, tag);
                }
                case "tag_or_block" -> {
                    var block = BLOCKS.getEntry(new ResourceLocation(GsonHelper.getAsString(jo, "block")));
                    var tag = AllTags.block(new ResourceLocation(GsonHelper.getAsString(jo, "tag")));
                    return $ -> $.checkBlock(ch, blockState -> blockState.is(block.get()) || blockState.is(tag));
                }
            }
            throw new UnsupportedTypeException("defines", type);
        }

        private <P> Multiblock.Builder<P> buildSpec(Multiblock.Builder<P> builder) {
            var spec = builder.spec();

            var ja1 = GsonHelper.getAsJsonArray(jo, "layers");
            for (var je : ja1) {
                spec.transform(parseLayer(je));
            }

            var jo2 = GsonHelper.getAsJsonObject(jo, "defines");
            for (var entry : jo2.entrySet()) {
                var ch = entry.getKey().charAt(0);
                spec.transform(parseDefine(ch, entry.getValue()));
            }

            return spec.build();
        }

        @Override
        public void run() {
            parseTypes();

            recipeType = getRecipeType();
            var layout = jo.has("layout") ? parseLayout().buildLayout() : Layout.EMPTY;

            var appearance = new ResourceLocation(GsonHelper.getAsString(jo, "appearance"));
            var block = BlockEntityBuilder.builder("multiblock/" + id, PrimitiveBlock::new)
                .translucent()
                .blockEntity()
                .child(this::multiblock)
                .layout(layout)
                .appearanceBlock(BLOCKS.getEntry(appearance))
                .transform(this::buildSpec)
                .build()
                .end()
                .buildObject();

            var set = new MultiblockSet(recipeType, layout, block);
            MULTIBLOCK_SETS.put(id, set);
            PROCESSING_TYPES.add(new RecipeTypeInfo(recipeType, layout, block));
        }
    }

    @Override
    protected MachineMeta.Executor getExecutor(ResourceLocation loc, JsonObject jo) {
        return new Executor(loc, jo);
    }
}
