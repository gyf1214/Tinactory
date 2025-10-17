package org.shsts.tinactory.content.machine;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.AllMenus;
import org.shsts.tinactory.content.electric.BatteryBox;
import org.shsts.tinactory.content.logistics.FlexibleStackContainer;
import org.shsts.tinactory.content.logistics.LogisticWorker;
import org.shsts.tinactory.content.logistics.StackProcessingContainer;
import org.shsts.tinactory.content.material.ComponentMeta;
import org.shsts.tinactory.content.multiblock.DigitalInterface;
import org.shsts.tinactory.content.network.MachineBlock;
import org.shsts.tinactory.content.network.PrimitiveBlock;
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe;
import org.shsts.tinactory.content.recipe.ChemicalReactorRecipe;
import org.shsts.tinactory.content.recipe.CleanRecipe;
import org.shsts.tinactory.content.recipe.DistillationRecipe;
import org.shsts.tinactory.content.recipe.EngravingRecipe;
import org.shsts.tinactory.content.recipe.GeneratorRecipe;
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe;
import org.shsts.tinactory.core.builder.BlockEntityBuilder;
import org.shsts.tinactory.core.common.MetaConsumer;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.LayoutSetBuilder;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.multiblock.MultiblockInterface;
import org.shsts.tinactory.core.multiblock.MultiblockInterfaceBlock;
import org.shsts.tinactory.core.multiblock.client.MultiblockInterfaceRenderer;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinactory.core.recipe.DisplayInputRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ResearchRecipe;
import org.shsts.tinactory.core.util.LocHelper;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllBlockEntities.MACHINE_SETS;
import static org.shsts.tinactory.content.AllRecipes.putTypeInfo;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineMeta extends MetaConsumer {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Transformer<BlockBehaviour.Properties> MACHINE_PROPERTY =
        $ -> $.strength(2f, 6f).sound(SoundType.METAL);

    public MachineMeta() {
        super("Machine");
    }

    protected MachineMeta(String name) {
        super(name);
    }

    private static void parseImage(JsonObject jo, int sh, BiConsumer<Rect, Texture> cons) {
        var x = GsonHelper.getAsInt(jo, "x");
        var y = GsonHelper.getAsInt(jo, "y");
        var w = GsonHelper.getAsInt(jo, "width");
        var h = GsonHelper.getAsInt(jo, "height");
        var texLoc = new ResourceLocation(GsonHelper.getAsString(jo, "texture"));
        var tw = GsonHelper.getAsInt(jo, "textureWidth", w);
        var th = GsonHelper.getAsInt(jo, "textureHeight", sh * h);
        var tex = new Texture(texLoc, tw, th);
        cons.accept(new Rect(x, y, w, h), tex);
    }

    public static LayoutSetBuilder<?> parseLayout(JsonObject jo) {
        var builder = Layout.builder();

        var ja1 = GsonHelper.getAsJsonArray(jo, "slots");
        for (var je1 : ja1) {
            var jo2 = GsonHelper.convertToJsonObject(je1, "slots");
            var port = GsonHelper.getAsInt(jo2, "port");
            var type = SlotType.fromName(GsonHelper.getAsString(jo2, "type"));
            var x = GsonHelper.getAsInt(jo2, "x");
            var y = GsonHelper.getAsInt(jo2, "y");
            Collection<Voltage> voltages;
            if (jo2.has("voltages")) {
                voltages = ComponentMeta.parseVoltage(jo2, "voltages");
            } else if (jo2.has("levels")) {
                var str = GsonHelper.getAsString(jo2, "levels");
                if (str.contains("-")) {
                    var fields = str.split("-");
                    var low = Voltage.fromRank(Integer.parseInt(fields[0]));
                    var high = fields.length > 1 ? Voltage.fromRank(Integer.parseInt(fields[1])) :
                        Voltage.MAX;
                    voltages = Voltage.between(low, high);
                } else {
                    voltages = List.of(Voltage.fromRank(Integer.parseInt(str)));
                }
            } else {
                voltages = Arrays.asList(Voltage.values());
            }
            builder.slot(port, type, x, y, voltages);
        }

        var ja3 = GsonHelper.getAsJsonArray(jo, "images", new JsonArray());
        for (var je3 : ja3) {
            var jo3 = GsonHelper.convertToJsonObject(je3, "images");
            parseImage(jo3, 1, builder::image);
        }

        if (jo.has("progressBar")) {
            var jo4 = GsonHelper.getAsJsonObject(jo, "progressBar");
            parseImage(jo4, 2, builder::progressBar);
        }

        return builder;
    }

    protected static class Executor implements Runnable {
        protected final String id;
        protected final JsonObject jo;

        protected String recipeTypeStr;
        protected String recipeTypeId;
        protected String machineType;
        @Nullable
        protected IRecipeType<?> recipeType;
        private IMenuType menu;
        private Map<Voltage, Layout> layoutSet;

        public Executor(ResourceLocation loc, JsonObject jo) {
            this.id = loc.getPath();
            this.jo = jo;
        }

        protected LayoutSetBuilder<?> parseLayout() {
            return MachineMeta.parseLayout(GsonHelper.getAsJsonObject(jo, "layout"));
        }

        private IRecipeType<ProcessingRecipe.Builder> processingRecipe(
            IRecipeType.BuilderFactory<ProcessingRecipe.Builder> builderFactory,
            Class<? extends ProcessingRecipe> clazz) {
            return REGISTRATE.recipeType(recipeTypeId, builderFactory)
                .recipeClass(clazz)
                .serializer(ProcessingRecipe.SERIALIZER)
                .register();
        }

        protected void parseRecipeType() {
            recipeType = switch (recipeTypeStr) {
                case "default" -> processingRecipe(ProcessingRecipe.Builder::new, ProcessingRecipe.class);
                case "display_input" -> processingRecipe(DisplayInputRecipe::builder, DisplayInputRecipe.class);
                case "generator" -> REGISTRATE.recipeType(recipeTypeId, GeneratorRecipe.Builder::new)
                    .recipeClass(GeneratorRecipe.class)
                    .serializer(GeneratorRecipe.SERIALIZER)
                    .register();
                case "distillation" -> processingRecipe(DistillationRecipe::builder, DistillationRecipe.class);
                case "research" -> REGISTRATE.recipeType(recipeTypeId, ResearchRecipe.Builder::new)
                    .recipeClass(ResearchRecipe.class)
                    .serializer(ResearchRecipe.SERIALIZER)
                    .register();
                case "assembly" -> REGISTRATE.recipeType(recipeTypeId, AssemblyRecipe.Builder::new)
                    .recipeClass(AssemblyRecipe.class)
                    .serializer(AssemblyRecipe.SERIALIZER)
                    .register();
                case "clean" -> REGISTRATE.recipeType(recipeTypeId, CleanRecipe.Builder::new)
                    .recipeClass(CleanRecipe.class)
                    .serializer(CleanRecipe.SERIALIZER)
                    .register();
                case "engraving" -> REGISTRATE.recipeType(recipeTypeId, EngravingRecipe::builder)
                    .recipeClass(EngravingRecipe.class)
                    .serializer(CleanRecipe.SERIALIZER)
                    .register();
                case "ore_analyzer" -> REGISTRATE.recipeType(recipeTypeId, OreAnalyzerRecipe.Builder::new)
                    .recipeClass(OreAnalyzerRecipe.class)
                    .serializer(OreAnalyzerRecipe.SERIALIZER)
                    .register();
                case "chemical_reactor" -> REGISTRATE.recipeType(recipeTypeId, ChemicalReactorRecipe.Builder::new)
                    .recipeClass(ChemicalReactorRecipe.class)
                    .serializer(ChemicalReactorRecipe.SERIALIZER)
                    .register();
                case "blast_furnace" -> REGISTRATE.recipeType(recipeTypeId, BlastFurnaceRecipe.Builder::new)
                    .recipeClass(BlastFurnaceRecipe.class)
                    .serializer(BlastFurnaceRecipe.SERIALIZER)
                    .register();
                case "electric_furnace", "none" -> null;
                default -> throw new UnsupportedTypeException("recipe", recipeTypeStr);
            };
        }

        @SuppressWarnings("unchecked")
        protected <R extends ProcessingRecipe, B extends IRecipeBuilderBase<R>> IRecipeType<B> recipeType() {
            assert recipeType != null;
            return (IRecipeType<B>) recipeType;
        }

        private IMenuType getMenu() {
            if (recipeTypeStr.equals("research")) {
                return AllMenus.RESEARCH_BENCH;
            }
            return AllMenus.PROCESSING_MACHINE;
        }

        private Layout getLayout(Voltage v) {
            return layoutSet.get(v);
        }

        private IEntry<PrimitiveBlock> primitive() {
            var machineId = "primitive/" + id;
            var processor = RecipeProcessors.processing(recipeType());
            return BlockEntityBuilder.builder(machineId, PrimitiveBlock::new)
                .menu(AllMenus.PRIMITIVE_MACHINE)
                .blockEntity()
                .transform(PrimitiveMachine::factory)
                .transform(RecipeProcessors.machine(List.of(processor), true))
                .transform(StackProcessingContainer.factory(getLayout(Voltage.PRIMITIVE)))
                .end()
                .block()
                .material(Material.WOOD)
                .properties($ -> $.strength(2f).sound(SoundType.WOOD))
                .translucent()
                .end()
                .buildObject();
        }

        private <P> IBlockEntityTypeBuilder<P> processor(IBlockEntityTypeBuilder<P> builder) {
            var processor = switch (recipeTypeStr) {
                case "electric_furnace" -> RecipeProcessors.electricFurnace(
                    GsonHelper.getAsInt(jo, "inputPort"),
                    GsonHelper.getAsInt(jo, "outputPort"),
                    GsonHelper.getAsDouble(jo, "amperage"));
                case "ore_analyzer" -> RecipeProcessors.oreAnalyzer(recipeType());
                case "generator" -> RecipeProcessors.generator(recipeType());
                default -> RecipeProcessors.processing(recipeType());
            };
            var autoRecipe = switch (machineType) {
                case "no_auto_recipe" -> false;
                case "default" -> true;
                default -> throw new UnsupportedTypeException("machine", machineType);
            };
            return builder.transform(RecipeProcessors.machine(List.of(processor), autoRecipe));
        }

        private String machineId(Voltage v) {
            if (id.contains("/")) {
                var name = LocHelper.name(id, -1);
                var base = id.substring(0, id.length() - name.length());
                return base + v.id + "/" + name;
            } else {
                return "machine/" + v.id + "/" + id;
            }
        }

        private BlockEntityBuilder<MachineBlock, ?> baseMachine(Voltage v) {
            return BlockEntityBuilder.builder(machineId(v), MachineBlock.factory(v))
                .transform(MachineSet::baseMachine)
                .block()
                .tint(i -> i == 2 ? v.color : 0xFFFFFFFF)
                .end();
        }

        private IEntry<MachineBlock> processing(Voltage v) {
            return baseMachine(v)
                .menu(menu)
                .blockEntity()
                .transform(StackProcessingContainer.factory(getLayout(v)))
                .transform(this::processor)
                .end()
                .buildObject();
        }

        private BlockEntityBuilder<MachineBlock, ?> baseInterface(Voltage v) {
            return BlockEntityBuilder.builder(machineId(v), MachineBlock.multiblockInterface(v))
                .blockEntity()
                .renderer(() -> () -> MultiblockInterfaceRenderer::new)
                .end()
                .block()
                .material(Material.HEAVY_METAL)
                .properties(MACHINE_PROPERTY)
                .tint(() -> () -> (state, $2, $3, i) ->
                    MultiblockInterfaceBlock.tint(v, state, i))
                .translucent()
                .end();
        }

        private IEntry<MachineBlock> multiblockInterface(Voltage v) {
            return baseInterface(v)
                .blockEntity()
                .transform(MultiblockInterface::factory)
                .transform(FlexibleStackContainer::factory)
                .end()
                .buildObject();
        }

        private IEntry<MachineBlock> digitalInterface(Voltage v, JsonObject jo) {
            var properties = new DigitalInterface.Properties(
                GsonHelper.getAsInt(jo, "maxParallel"),
                GsonHelper.getAsInt(jo, "bytesLimit"),
                GsonHelper.getAsInt(jo, "dedicatedBytes"),
                GsonHelper.getAsInt(jo, "amountByteLimit"));

            return baseInterface(v)
                .blockEntity()
                .transform(DigitalInterface.factory(properties))
                .end()
                .buildObject();
        }

        private IEntry<MachineBlock> batteryBox(Voltage v) {
            return BlockEntityBuilder.builder(machineId(v), MachineBlock.sided(v))
                .transform(MachineSet::baseMachine)
                .menu(AllMenus.SIMPLE_MACHINE)
                .blockEntity()
                .transform(BatteryBox.factory(getLayout(v)))
                .end()
                .block()
                .tint(i -> i == 0 ? v.color : 0xFFFFFFFF)
                .end()
                .buildObject();
        }

        private double getPower(Voltage v, JsonObject jo) {
            if (jo.has("amperage")) {
                return v.value * GsonHelper.getAsDouble(jo, "amperage");
            } else {
                return GsonHelper.getAsDouble(jo, "power");
            }
        }

        private IEntry<MachineBlock> electricChest(Voltage v, JsonObject jo) {
            return BlockEntityBuilder.builder(machineId(v), MachineBlock::simple)
                .transform(MachineSet::baseMachine)
                .menu(AllMenus.ELECTRIC_CHEST)
                .blockEntity()
                .transform(ElectricChest.factory(getLayout(v), getPower(v, jo)))
                .end()
                .buildObject();
        }

        private IEntry<MachineBlock> electricTank(Voltage v, JsonObject jo) {
            return BlockEntityBuilder.builder(machineId(v), MachineBlock::simple)
                .transform(MachineSet::baseMachine)
                .menu(AllMenus.ELECTRIC_TANK)
                .blockEntity()
                .transform(ElectricTank.factory(getLayout(v), getPower(v, jo)))
                .end()
                .buildObject();
        }

        private IEntry<MachineBlock> logisticWorker(Voltage v, JsonObject jo) {
            var properties = new LogisticWorker.Properties(
                GsonHelper.getAsInt(jo, "slots"),
                GsonHelper.getAsInt(jo, "interval"),
                GsonHelper.getAsInt(jo, "stack"),
                GsonHelper.getAsInt(jo, "fluidStack"),
                getPower(v, jo));

            return BlockEntityBuilder.builder(machineId(v), MachineBlock::simple)
                .transform(MachineSet::baseMachine)
                .menu(AllMenus.LOGISTIC_WORKER)
                .blockEntity()
                .transform(LogisticWorker.factory(properties))
                .end()
                .buildObject();
        }

        private IEntry<? extends Block> buildMachine(Voltage v, JsonObject jo) {
            var basic = switch (machineType) {
                case "multiblock_interface" -> multiblockInterface(v);
                case "digital_interface" -> digitalInterface(v, jo);
                case "battery_box" -> batteryBox(v);
                case "electric_chest" -> electricChest(v, jo);
                case "electric_tank" -> electricTank(v, jo);
                case "logistic_worker" -> logisticWorker(v, jo);
                default -> null;
            };
            if (basic != null) {
                return basic;
            }

            if (v == Voltage.PRIMITIVE) {
                return primitive();
            }
            return processing(v);
        }

        protected void parseTypes() {
            recipeTypeStr = GsonHelper.getAsString(jo, "recipe", "default");
            machineType = GsonHelper.getAsString(jo, "machine", "default");
            if (jo.has("recipeTypeId")) {
                recipeTypeId = GsonHelper.getAsString(jo, "recipeTypeId");
            } else {
                recipeTypeId = id;
            }
        }

        @Override
        public void run() {
            parseTypes();

            parseRecipeType();
            layoutSet = jo.has("layout") ? parseLayout().buildObject() : Layout.EMPTY_SET;
            menu = getMenu();

            var machines = new HashMap<Voltage, IEntry<? extends Block>>();
            IEntry<? extends Block> icon = null;
            for (var entry : ComponentMeta.parseVoltageConfig(jo, "voltages")) {
                var v = entry.voltage();
                var block = buildMachine(v, entry.jo());
                if (v.rank > Voltage.ULV.rank && icon == null) {
                    icon = block;
                }
                machines.put(v, block);
            }

            if (recipeType != null) {
                MACHINE_SETS.put(id, new ProcessingSet(recipeType, layoutSet, machines));
            } else {
                MACHINE_SETS.put(id, new MachineSet(layoutSet, machines));
            }

            if (recipeType != null && !recipeTypeStr.equals("chemical_reactor") && icon != null) {
                putTypeInfo(recipeType, layoutSet.get(Voltage.MAX), icon);
            }
        }
    }

    protected Runnable getExecutor(ResourceLocation loc, JsonObject jo) {
        return new Executor(loc, jo);
    }

    @Override
    protected void doAcceptMeta(ResourceLocation loc, JsonObject jo) {
        try {
            getExecutor(loc, jo).run();
        } catch (UnsupportedTypeException ex) {
            LOGGER.debug("Skip unsupported type: {}", loc, ex);
        }
    }
}
