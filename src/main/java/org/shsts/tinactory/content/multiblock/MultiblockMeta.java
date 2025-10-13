package org.shsts.tinactory.content.multiblock;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Material;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.machine.MachineMeta;
import org.shsts.tinactory.content.machine.UnsupportedTypeException;
import org.shsts.tinactory.content.network.PrimitiveBlock;
import org.shsts.tinactory.core.builder.BlockEntityBuilder;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.machine.IRecipeProcessor;
import org.shsts.tinactory.core.machine.RecipeProcessors;
import org.shsts.tinactory.core.multiblock.Multiblock;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllMultiblocks.MULTIBLOCK_SETS;
import static org.shsts.tinactory.content.AllRecipes.putTypeInfo;
import static org.shsts.tinactory.content.AllRegistries.BLOCKS;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockMeta extends MachineMeta {
    public MultiblockMeta() {
        super("Multiblock");
    }

    private static class Executor extends MachineMeta.Executor {
        private final List<Supplier<? extends IRecipeProcessor<?>>> processors = new ArrayList<>();
        private final List<IRecipeType<?>> recipeTypes = new ArrayList<>();

        public Executor(ResourceLocation loc, JsonObject jo) {
            super(loc, jo);
        }

        private <P> Multiblock.Builder<P> multiblock(IBlockEntityTypeBuilder<P> builder) {
            var autoRecipe = GsonHelper.getAsBoolean(jo, "autoRecipe", true);
            builder.transform(RecipeProcessors.multiblock(processors, autoRecipe));

            return switch (machineType) {
                case "default" -> builder.child(Multiblock.builder(Multiblock::new));
                case "research" -> builder.child(Multiblock.builder(ResearchMultiblock::new));
                case "coil", "blast_furnace" -> builder.child(Multiblock.builder(CoilMultiblock::new));
                case "engraving" -> builder.child(Multiblock.builder(Lithography::new));
                case "distillation" -> {
                    var maxHeight = GsonHelper.getAsInt(jo, "maxHeight");
                    var layouts = parseLayout().buildList(maxHeight - 2);
                    yield builder.child(Multiblock.builder((be, $) -> new DistillationTower(be, $, layouts)));
                }
                case "large_turbine" -> builder.child(Multiblock.builder(LargeTurbine::new));
                default -> {
                    if (machineType.equals(recipeTypeStr)) {
                        yield builder.child(Multiblock.builder(Multiblock::new));
                    } else {
                        throw new UnsupportedTypeException("machine", machineType);
                    }
                }
            };
        }

        @Override
        protected void parseRecipeType() {
            if (recipeTypeStr.contains(":")) {
                recipeType = REGISTRATE.getRecipeType(new ResourceLocation(recipeTypeStr));
            } else {
                super.parseRecipeType();
            }
            if (recipeType != null) {
                recipeTypes.add(recipeType);
            }
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

        private Supplier<? extends IRecipeProcessor<?>> getProcessor(JsonObject jo, String machineType) {
            if (recipeTypeStr.equals("electric_furnace")) {
                return RecipeProcessors.electricFurnace(
                    GsonHelper.getAsInt(jo, "inputPort"),
                    GsonHelper.getAsInt(jo, "outputPort"),
                    GsonHelper.getAsDouble(jo, "amperage"),
                    GsonHelper.getAsInt(jo, "baseTemperature", 0));
            } else {
                parseRecipeType();
                return switch (machineType) {
                    case "default", "engraving" -> RecipeProcessors.processing(recipeType());
                    case "coil" -> {
                        var baseTemp = GsonHelper.getAsInt(jo, "baseTemperature");
                        yield RecipeProcessors.coil(recipeType(), baseTemp);
                    }
                    case "blast_furnace" -> RecipeProcessors.blastFurnace(recipeType());
                    case "ore_analyzer" -> RecipeProcessors.oreAnalyzer(recipeType());
                    case "generator" -> RecipeProcessors.generator(recipeType());
                    default -> {
                        if (machineType.equals(recipeTypeStr)) {
                            yield RecipeProcessors.processing(recipeType());
                        } else {
                            throw new UnsupportedTypeException("machine", machineType);
                        }
                    }
                };
            }
        }

        @Override
        public void run() {
            parseTypes();

            if (jo.has("recipes")) {
                var jo1 = GsonHelper.getAsJsonObject(jo, "recipes");
                for (var entry : jo1.entrySet()) {
                    recipeTypeStr = entry.getKey();
                    var jo2 = GsonHelper.convertToJsonObject(entry.getValue(), "recipes");
                    var defaultType = recipeTypeStr.contains(":") ? "default" : recipeTypeStr;
                    var machineType = GsonHelper.getAsString(jo2, "machine", defaultType);
                    processors.add(getProcessor(jo2, machineType));
                }
            } else {
                var defaultType = recipeTypeStr.contains(":") ? "default" : recipeTypeStr;
                machineType = GsonHelper.getAsString(jo, "machine", defaultType);
                processors.add(getProcessor(jo, machineType));
            }

            var layout = jo.has("layout") ? parseLayout().buildLayout() : Layout.EMPTY;

            var appearance = new ResourceLocation(GsonHelper.getAsString(jo, "appearance"));
            var block = BlockEntityBuilder.builder("multiblock/" + id, PrimitiveBlock::new)
                .blockEntity()
                .child(this::multiblock)
                .layout(layout)
                .appearanceBlock(BLOCKS.getEntry(appearance))
                .transform(this::buildSpec)
                .build()
                .end()
                .block()
                .material(Material.HEAVY_METAL)
                .properties(MACHINE_PROPERTY)
                .translucent()
                .end()
                .buildObject();

            var set = new MultiblockSet(recipeTypes, block);
            MULTIBLOCK_SETS.put(id, set);
            for (var recipeType : recipeTypes) {
                putTypeInfo(recipeType, layout, block);
            }
        }
    }

    @Override
    protected Runnable getExecutor(ResourceLocation loc, JsonObject jo) {
        return new Executor(loc, jo);
    }
}
