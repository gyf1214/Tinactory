package org.shsts.tinactory.content.multiblock;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.AllTags;
import org.shsts.tinactory.api.multiblock.IMultiblockCheckCtx;
import org.shsts.tinactory.content.machine.FireBoiler;
import org.shsts.tinactory.content.machine.MachineMeta;
import org.shsts.tinactory.content.machine.RecipeProcessors;
import org.shsts.tinactory.content.machine.UnsupportedTypeException;
import org.shsts.tinactory.content.network.FixedMachineBlock;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.machine.IRecipeProcessor;
import org.shsts.tinactory.core.multiblock.MultiblockSpec;
import org.shsts.tinactory.integration.builder.BlockEntityBuilder;
import org.shsts.tinactory.integration.multiblock.BlockIngredient;
import org.shsts.tinactory.integration.multiblock.BlockIngredient.BlockValue;
import org.shsts.tinactory.integration.multiblock.BlockIngredient.TagValue;
import org.shsts.tinactory.integration.multiblock.Multiblock;
import org.shsts.tinactory.integration.network.PrimitiveBlock;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.shsts.tinactory.AllMultiblocks.CLEANROOM_PROPERTIES;
import static org.shsts.tinactory.AllMultiblocks.LITHOGRAPHY_CLEANNESS_FACTORS;
import static org.shsts.tinactory.AllMultiblocks.MULTIBLOCK_SETS;
import static org.shsts.tinactory.AllRecipes.putTypeInfo;
import static org.shsts.tinactory.AllRegistries.BLOCKS;
import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockMeta extends MachineMeta {
    public MultiblockMeta() {
        super("Multiblock");
    }

    private static IEntry<Block> getBlock(JsonObject jo, String member) {
        var loc = ResourceLocation.parse(GsonHelper.getAsString(jo, member));
        return BLOCKS.getEntry(loc);
    }

    private static TagKey<Block> getBlockTag(JsonObject jo, String member) {
        var loc = ResourceLocation.parse(GsonHelper.getAsString(jo, member));
        return AllTags.block(loc);
    }

    private static void collectMandatorySymbols(Collection<String> rows, Consumer<Character> consumer) {
        for (var row : rows) {
            for (var i = 0; i < row.length(); i++) {
                var ch = row.charAt(i);
                if (ch != MultiblockSpec.IGNORED_CHAR && ch != MultiblockSpec.CENTER_CHAR) {
                    consumer.accept(ch);
                }
            }
        }
    }

    private static class Executor extends MachineMeta.Executor {
        private final List<Function<BlockEntity, ? extends IRecipeProcessor<?>>> processors = new ArrayList<>();
        private final List<IRecipeType<?>> recipeTypes = new ArrayList<>();
        private final Set<BlockIngredient> structureIngredients = new LinkedHashSet<>();

        public Executor(ResourceLocation loc, JsonObject jo) {
            super(loc, jo);
        }

        private <P> Multiblock.Builder<P> multiblock(IBlockEntityTypeBuilder<P> builder) {
            if (!processors.isEmpty()) {
                var autoRecipe = GsonHelper.getAsBoolean(jo, "autoRecipe", true);
                if (machineType.equals("fusion_reactor")) {
                    var properties = FusionRuntime.Properties.fromJson(jo);
                    builder.transform(RecipeProcessors.fusionMultiblock(processors, autoRecipe, properties));
                } else {
                    builder.transform(RecipeProcessors.multiblock(processors, autoRecipe));
                }
            }

            return switch (machineType) {
                case "default" -> builder.child(Multiblock.builder(Multiblock::new));
                case "research" -> builder.child(Multiblock.builder(ResearchMultiblock::new));
                case "coil", "blast_furnace" -> builder.child(Multiblock.builder(CoilMultiblock::new));
                case "engraving" -> {
                    var factor = GsonHelper.getAsDouble(jo, "cleannessFactor");
                    LITHOGRAPHY_CLEANNESS_FACTORS.put(id, factor);
                    yield builder.child(Multiblock.builder((be, $) -> new Lithography(be, $, factor)));
                }
                case "distillation" -> {
                    var minHeight = GsonHelper.getAsInt(jo, "minHeight");
                    var maxHeight = GsonHelper.getAsInt(jo, "maxHeight");
                    var layouts = parseLayout().buildList(maxHeight - minHeight + 1);
                    yield builder.child(Multiblock.builder((be, $) ->
                        new DistillationTower(be, $, layouts, minHeight)));
                }
                case "large_turbine" -> builder.child(Multiblock.builder(LargeTurbine::new));
                case "power_substation" -> builder.child(Multiblock.builder(PowerSubstation::new));
                case "large_boiler" -> {
                    var properties = FireBoiler.Properties.fromJson(jo);
                    var baseBoilerParallel = GsonHelper.getAsDouble(jo, "baseBoilerParallel");
                    yield builder.child(Multiblock.builder((be, $) ->
                        new LargeBoiler(be, $, properties, baseBoilerParallel)));
                }
                case "nuclear_reactor" -> {
                    var properties = NuclearReactor.Properties.fromJson(jo);
                    yield builder.child(Multiblock.builder((be, $) -> new NuclearReactor(be, $, properties)));
                }
                case "fusion_reactor" -> builder.child(Multiblock.builder(FusionReactor::new));
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
                recipeType = REGISTRATE.getRecipeType(ResourceLocation.parse(recipeTypeStr));
            } else {
                super.parseRecipeType();
            }
            if (recipeType != null) {
                recipeTypes.add(recipeType);
            }
        }

        private static BiConsumer<IMultiblockCheckCtx<BlockState>, BlockPos> blockStateCheck(
            Predicate<BlockState> pred) {
            return (ctx, pos) -> {
                var block = ctx.getBlock(pos);
                if (block.isEmpty() || !pred.test(block.get())) {
                    ctx.setFailed();
                }
            };
        }

        private static BiConsumer<IMultiblockCheckCtx<BlockState>, BlockPos> block(
            Supplier<? extends Block> block, boolean allowInterface) {
            return (ctx, pos) -> {
                var block1 = ctx.getBlock(pos);
                if (allowInterface && MultiblockSpec.checkInterface(ctx, pos)) {
                    return;
                }
                if (block1.isEmpty() || !block1.get().is(block.get())) {
                    ctx.setFailed();
                }
            };
        }

        private static BiConsumer<IMultiblockCheckCtx<BlockState>, BlockPos> tagWithSameBlock(String key,
            TagKey<Block> tag) {
            return (ctx, pos) -> {
                var block1 = ctx.getBlock(pos);
                if (block1.isEmpty() || !block1.get().is(tag)) {
                    ctx.setFailed();
                } else if (ctx.hasProperty(key)) {
                    if (!block1.get().is((Block) ctx.getProperty(key))) {
                        ctx.setFailed();
                    }
                } else {
                    ctx.setProperty(key, block1.get().getBlock());
                }
            };
        }

        private <P> Transformer<MultiblockSpec.Builder<BlockState, P>> parseLayer(JsonElement je,
            Consumer<Character> mandatorySymbolConsumer) {
            if (je.isJsonArray()) {
                var ja = je.getAsJsonArray();
                var rows = new ArrayList<String>();
                for (var je1 : ja) {
                    rows.add(GsonHelper.convertToString(je1, "rows"));
                }
                collectMandatorySymbols(rows, mandatorySymbolConsumer);
                return builder -> {
                    var layer = builder.layer();
                    for (var line : rows) {
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
                var rows = new ArrayList<String>();
                for (var je2 : ja2) {
                    rows.add(GsonHelper.convertToString(je2, "rows"));
                }
                if (minHeight > 0) {
                    collectMandatorySymbols(rows, mandatorySymbolConsumer);
                }
                return builder -> {
                    var layer = builder.layer().height(minHeight, maxHeight);
                    for (var line : rows) {
                        layer.row(line);
                    }
                    return layer.build();
                };
            }
            throw new JsonSyntaxException("Except field layers to be either JsonArray or JsonObject");
        }

        private <P> Transformer<MultiblockSpec.Builder<BlockState, P>> parseDefine(Character ch, JsonElement je,
            Consumer<BlockIngredient> ingredientConsumer) {
            if (je.isJsonPrimitive() && je.getAsJsonPrimitive().isString()) {
                var s = je.getAsString();
                if (s.equals("air")) {
                    return $ -> $.check(ch, blockStateCheck(BlockState::isAir));
                } else {
                    var block = BLOCKS.getEntry(ResourceLocation.parse(s));
                    ingredientConsumer.accept(BlockIngredient.of(block));
                    return $ -> $.check(ch, block(block, false));
                }
            }

            var jo = GsonHelper.convertToJsonObject(je, "defines");
            var type = GsonHelper.getAsString(jo, "type");
            switch (type) {
                case "block_or_interface" -> {
                    var block = getBlock(jo, "block");
                    ingredientConsumer.accept(BlockIngredient.of(block));
                    return $ -> $.check(ch, block(block, true));
                }
                case "tag" -> {
                    var tag = getBlockTag(jo, "tag");
                    ingredientConsumer.accept(BlockIngredient.of(tag));
                    return $ -> $.check(ch, blockStateCheck(blockState -> blockState.is(tag)));
                }
                case "tag_or_interface" -> {
                    var tag = getBlockTag(jo, "tag");
                    ingredientConsumer.accept(BlockIngredient.of(tag));
                    return $ -> $.check(ch, (ctx, pos) -> {
                        var block = ctx.getBlock(pos);
                        if (MultiblockSpec.checkInterface(ctx, pos)) {
                            return;
                        }
                        if (block.isEmpty() || !block.get().is(tag)) {
                            ctx.setFailed();
                        }
                    });
                }
                case "tag_with_same_block" -> {
                    var tag = getBlockTag(jo, "tag");
                    var key = GsonHelper.getAsString(jo, "key");
                    ingredientConsumer.accept(BlockIngredient.of(tag));
                    return $ -> $.check(ch, tagWithSameBlock(key, tag));
                }
                case "tag_or_block" -> {
                    var block = getBlock(jo, "block");
                    var tag = getBlockTag(jo, "tag");
                    ingredientConsumer.accept(BlockIngredient.of(new BlockValue(block), new TagValue(tag)));
                    return $ -> $.checkBlock(ch, blockState -> blockState.is(block.get()) || blockState.is(tag));
                }
            }
            throw new UnsupportedTypeException("defines", type);
        }

        private <P> Multiblock.Builder<P> buildSpec(Multiblock.Builder<P> builder) {
            var spec = builder.spec();
            Set<Character> mandatorySymbols = new LinkedHashSet<>();
            Map<Character, BlockIngredient> ingredientsBySymbol = new LinkedHashMap<>();

            var ja1 = GsonHelper.getAsJsonArray(jo, "layers");
            for (var je : ja1) {
                spec.transform(parseLayer(je, mandatorySymbols::add));
            }

            var jo2 = GsonHelper.getAsJsonObject(jo, "defines");
            for (var entry : jo2.entrySet()) {
                var ch = entry.getKey().charAt(0);
                spec.transform(parseDefine(ch, entry.getValue(),
                    ingredient -> ingredientsBySymbol.put(ch, ingredient)));
            }

            for (var ch : mandatorySymbols) {
                var ingredient = ingredientsBySymbol.get(ch);
                if (ingredient != null) {
                    structureIngredients.add(ingredient);
                }
            }

            return spec.build();
        }

        private Function<BlockEntity, ? extends IRecipeProcessor<?>> getProcessor(JsonObject jo, String machineType) {
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
                    case "fusion_reactor" -> RecipeProcessors.fusion(recipeType());
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

        private IEntry<? extends Block> buildCleanroom() {
            var properties = new Cleanroom.Properties(
                GsonHelper.getAsDouble(jo, "amperage"),
                GsonHelper.getAsDouble(jo, "baseClean"),
                GsonHelper.getAsDouble(jo, "baseDecay"),
                GsonHelper.getAsDouble(jo, "openDecay"));
            CLEANROOM_PROPERTIES.put(id, properties);

            var jo1 = GsonHelper.getAsJsonObject(jo, "spec");
            var baseBlock = getBlock(jo1, "base");
            var ceilingBlock = getBlock(jo1, "ceiling");
            var wallTag = getBlockTag(jo1, "wall");
            structureIngredients.add(BlockIngredient.of(baseBlock));
            structureIngredients.add(BlockIngredient.of(ceilingBlock));
            structureIngredients.add(BlockIngredient.of(wallTag));

            var layout = parseLayout().buildLayout();

            return BlockEntityBuilder.builder("multiblock/" + id, FixedMachineBlock::new)
                .blockEntity()
                .transform(this::sound)
                .child(Multiblock.builder((be, $) -> new Cleanroom(be, $, properties)))
                .layout(layout)
                .appearanceBlock(getBlock(jo, "appearance"))
                .spec(Cleanroom::spec)
                .baseBlock(baseBlock)
                .ceilingBlock(ceilingBlock)
                .wallTag(wallTag)
                .connectorTag(getBlockTag(jo1, "connector"))
                .doorTag(getBlockTag(jo1, "door"))
                .maxSize(GsonHelper.getAsInt(jo1, "maxSize"))
                .maxHeight(GsonHelper.getAsInt(jo1, "maxHeight"))
                .maxConnectors(GsonHelper.getAsInt(jo1, "maxConnectors"))
                .maxDoors(GsonHelper.getAsInt(jo1, "maxDoors"))
                .build()
                .end()
                .block()
                .properties(MACHINE_PROPERTY)
                .end()
                .buildObject();
        }

        private IEntry<? extends Block> buildBlock() {
            if (machineType.equals("cleanroom")) {
                return buildCleanroom();
            }

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

            var block = BlockEntityBuilder.builder("multiblock/" + id, PrimitiveBlock::new)
                .blockEntity()
                .transform(this::sound)
                .child(this::multiblock)
                .transform(machineType.equals("distillation") ? $ -> $ : $ -> $.layout(layout))
                .appearanceBlock(getBlock(jo, "appearance"))
                .transform(this::buildSpec)
                .build()
                .end()
                .block()
                .properties(MACHINE_PROPERTY)
                .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                .end()
                .buildObject();

            for (var recipeType : recipeTypes) {
                putTypeInfo(recipeType, layout, block);
            }

            return block;
        }

        @Override
        public void run() {
            parseTypes();

            var block = buildBlock();

            var set = new MultiblockSet(recipeTypes, block, List.copyOf(structureIngredients));
            MULTIBLOCK_SETS.put(id, set);
        }
    }

    @Override
    protected Runnable getExecutor(ResourceLocation loc, JsonObject jo) {
        return new Executor(loc, jo);
    }
}
