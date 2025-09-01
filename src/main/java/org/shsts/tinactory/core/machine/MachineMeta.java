package org.shsts.tinactory.core.machine;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.AllMenus;
import org.shsts.tinactory.content.logistics.StackProcessingContainer;
import org.shsts.tinactory.content.machine.PrimitiveMachine;
import org.shsts.tinactory.content.machine.ProcessingSet;
import org.shsts.tinactory.content.network.MachineBlock;
import org.shsts.tinactory.content.network.PrimitiveBlock;
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe;
import org.shsts.tinactory.content.recipe.ChemicalReactorRecipe;
import org.shsts.tinactory.content.recipe.CleanRecipe;
import org.shsts.tinactory.content.recipe.DistillationRecipe;
import org.shsts.tinactory.content.recipe.GeneratorRecipe;
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe;
import org.shsts.tinactory.core.builder.BlockEntityBuilder;
import org.shsts.tinactory.core.common.MetaConsumer;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.LayoutSetBuilder;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinactory.core.recipe.DisplayInputRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ResearchRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllBlockEntities.MACHINE_SETS;
import static org.shsts.tinactory.content.AllBlockEntities.PROCESSING_SETS;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineMeta extends MetaConsumer {
    private static final Logger LOGGER = LogUtils.getLogger();

    public MachineMeta() {
        super("Machine");
    }

    private static class UnsupportedTypeException extends RuntimeException {
        public UnsupportedTypeException(String field, String value) {
            super("Unsupported " + field + ": " + value);
        }
    }

    private static class Executor {
        private final String id;
        private final JsonObject jo;

        private String recipeTypeStr;
        private String machineType;
        private String menuType;

        private IRecipeType<?> recipeType;
        private IMenuType menu;
        private Map<Voltage, Layout> layoutSet;

        public Executor(ResourceLocation loc, JsonObject jo) {
            this.id = loc.getPath();
            this.jo = jo;
        }

        private IRecipeType<ProcessingRecipe.Builder> processingRecipe(
            IRecipeType.BuilderFactory<ProcessingRecipe.Builder> builderFactory) {
            return REGISTRATE.recipeType(id, builderFactory)
                .recipeClass(ProcessingRecipe.class)
                .serializer(ProcessingRecipe.SERIALIZER)
                .register();
        }

        private IRecipeType<?> getRecipeType() {
            return switch (recipeTypeStr) {
                case "default" -> processingRecipe(ProcessingRecipe.Builder::new);
                case "display_input" -> processingRecipe(DisplayInputRecipe::builder);
                case "generator" -> processingRecipe(GeneratorRecipe::builder);
                case "distillation" -> processingRecipe(DistillationRecipe::builder);
                case "research" -> REGISTRATE.recipeType(id, ResearchRecipe.Builder::new)
                    .recipeClass(ResearchRecipe.class)
                    .serializer(ResearchRecipe.SERIALIZER)
                    .register();
                case "assembly" -> REGISTRATE.recipeType(id, AssemblyRecipe.Builder::new)
                    .recipeClass(AssemblyRecipe.class)
                    .serializer(AssemblyRecipe.SERIALIZER)
                    .register();
                case "clean" -> REGISTRATE.recipeType(id, CleanRecipe.Builder::new)
                    .recipeClass(CleanRecipe.class)
                    .serializer(CleanRecipe.SERIALIZER)
                    .register();
                case "ore_analyzer" -> REGISTRATE.recipeType(id, OreAnalyzerRecipe.Builder::new)
                    .recipeClass(OreAnalyzerRecipe.class)
                    .serializer(OreAnalyzerRecipe.SERIALIZER)
                    .register();
                case "chemical_reactor" -> REGISTRATE.recipeType(id, ChemicalReactorRecipe.Builder::new)
                    .recipeClass(ChemicalReactorRecipe.class)
                    .serializer(ChemicalReactorRecipe.SERIALIZER)
                    .register();
                case "blast_furnace" -> REGISTRATE.recipeType(id, BlastFurnaceRecipe.Builder::new)
                    .recipeClass(BlastFurnaceRecipe.class)
                    .serializer(BlastFurnaceRecipe.SERIALIZER)
                    .register();
                default -> throw new UnsupportedTypeException("recipe", recipeTypeStr);
            };
        }

        @SuppressWarnings("unchecked")
        private <R extends ProcessingRecipe, B extends IRecipeBuilderBase<R>> IRecipeType<B> recipeType() {
            return (IRecipeType<B>) recipeType;
        }

        private IMenuType getMenu() {
            switch (recipeTypeStr) {
                case "ore_analyzer" -> {
                    return AllMenus.MARKER;
                }
                case "research" -> {
                    return AllMenus.RESEARCH_BENCH;
                }
            }
            return switch (menuType) {
                case "default" -> AllMenus.PROCESSING_MACHINE;
                case "marker" -> AllMenus.MARKER;
                case "marker_with_normal" -> AllMenus.MARKER_WITH_NORMAL;
                default -> throw new UnsupportedTypeException("menu", menuType);
            };
        }

        private Layout getLayout(Voltage v) {
            return layoutSet.get(v);
        }

        private IEntry<PrimitiveBlock> primitive() {
            var machineId = "primitive/" + id;
            return BlockEntityBuilder.builder(machineId, PrimitiveBlock::new)
                .menu(AllMenus.PRIMITIVE_MACHINE)
                .blockEntity()
                .transform(PrimitiveMachine::factory)
                .transform(RecipeProcessor.machine(recipeType()))
                .transform(StackProcessingContainer.factory(getLayout(Voltage.PRIMITIVE)))
                .end()
                .translucent()
                .buildObject();
        }

        private <P> IBlockEntityTypeBuilder<P> processor(IBlockEntityTypeBuilder<P> builder) {
            if (recipeTypeStr.equals("ore_analyzer")) {
                return builder.transform(RecipeProcessor.oreAnalyzer(recipeType()));
            } else if (recipeTypeStr.equals("generator")) {
                return builder.transform(RecipeProcessor.generator(recipeType()));
            } else if (machineType.equals("no_auto_recipe")) {
                return builder.transform(RecipeProcessor.noAutoRecipe(recipeType()));
            } else {
                return builder.transform(RecipeProcessor.machine(recipeType()));
            }
        }

        private IEntry<MachineBlock> processing(Voltage v) {
            var machineId = "machine/" + v.id + "/" + id;
            var builder = BlockEntityBuilder.builder(machineId, MachineBlock.factory(v));
            return builder.menu(menu)
                .blockEntity()
                .transform(Machine::factory)
                .transform(StackProcessingContainer.factory(getLayout(v)))
                .transform(this::processor)
                .end()
                .block()
                .translucent()
                .tint(i -> i == 2 ? v.color : 0xFFFFFFFF)
                .end()
                .buildObject();
        }

        private IEntry<? extends Block> buildMachine(Voltage v) {
            if (v == Voltage.PRIMITIVE) {
                return primitive();
            }
            return processing(v);
        }

        public void run() {
            recipeTypeStr = GsonHelper.getAsString(jo, "recipe", "default");
            menuType = GsonHelper.getAsString(jo, "menu", "default");
            machineType = GsonHelper.getAsString(jo, "machine", "default");

            recipeType = getRecipeType();
            layoutSet = LayoutSetBuilder.fromJson(GsonHelper.getAsJsonObject(jo, "layout")).buildObject();
            menu = getMenu();

            var machines = new HashMap<Voltage, IEntry<? extends Block>>();
            for (var v : Voltage.parseJson(jo, "voltages")) {
                machines.put(v, buildMachine(v));
            }

            var set = new ProcessingSet(recipeType, layoutSet, machines);
            PROCESSING_SETS.add(set);
            MACHINE_SETS.put(id, set);
        }
    }

    @Override
    protected void doAcceptMeta(ResourceLocation loc, JsonObject jo) {
        try {
            new Executor(loc, jo).run();
        } catch (UnsupportedTypeException ex) {
            LOGGER.debug("Skip unsupported type: " + loc, ex);
        }
    }
}
