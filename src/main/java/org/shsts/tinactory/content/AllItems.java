package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import org.shsts.tinactory.content.machine.MachineSet;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.material.ComponentSet;
import org.shsts.tinactory.content.model.CableModel;
import org.shsts.tinactory.content.model.MachineModel;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.content.network.CableBlock;
import org.shsts.tinactory.core.common.SimpleFluid;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.function.Supplier;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllBlockEntities.ALLOY_SMELTER;
import static org.shsts.tinactory.content.AllBlockEntities.ASSEMBLER;
import static org.shsts.tinactory.content.AllBlockEntities.ELECTRIC_FURNACE;
import static org.shsts.tinactory.content.AllBlockEntities.LOW_PRESSURE_BOILER;
import static org.shsts.tinactory.content.AllBlockEntities.NETWORK_CONTROLLER;
import static org.shsts.tinactory.content.AllBlockEntities.ORE_ANALYZER;
import static org.shsts.tinactory.content.AllBlockEntities.ORE_WASHER;
import static org.shsts.tinactory.content.AllBlockEntities.RESEARCH_TABLE;
import static org.shsts.tinactory.content.AllBlockEntities.STEAM_TURBINE;
import static org.shsts.tinactory.content.AllBlockEntities.STONE_GENERATOR;
import static org.shsts.tinactory.content.AllBlockEntities.WORKBENCH;
import static org.shsts.tinactory.content.AllMaterials.ALUMINIUM;
import static org.shsts.tinactory.content.AllMaterials.BRONZE;
import static org.shsts.tinactory.content.AllMaterials.COPPER;
import static org.shsts.tinactory.content.AllMaterials.CUPRONICKEL;
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllMaterials.STEEL;
import static org.shsts.tinactory.content.AllMaterials.TIN;
import static org.shsts.tinactory.content.AllRecipes.TOOL;
import static org.shsts.tinactory.content.AllRecipes.has;
import static org.shsts.tinactory.content.model.ModelGen.gregtech;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllItems {
    public static final RegistryEntry<CableBlock> ULV_CABLE;
    public static final RegistryEntry<Item> ULV_MACHINE_HULL;
    public static final RegistryEntry<Item> ULV_RESEARCH_EQUIPMENT;
    public static final Map<Voltage, ComponentSet> COMPONENT_SETS;
    public static final RegistryEntry<Item> VACUUM_TUBE;
    public static final RegistryEntry<SimpleFluid> STEAM;
    public static final RegistryEntry<Block> HEAT_PROOF_BLOCK;

    static {
        ULV_CABLE = REGISTRATE.block("network/cable/ulv",
                        properties -> new CableBlock(properties, CableBlock.WIRE_RADIUS, Voltage.ULV, 2.0))
                .blockState(ctx -> CableModel.blockState(ctx, true))
                .tint(IRON.color)
                .tag(AllTags.MINEABLE_WITH_CUTTER)
                .defaultBlockItem(CableModel::ulvCableModel).dropSelf()
                .register();

        ULV_MACHINE_HULL = REGISTRATE.item("component/ulv/machine_hull", Item::new)
                .model(ModelGen.machineItem(Voltage.ULV, gregtech(MachineModel.IO_TEX)))
                .register();

        ULV_RESEARCH_EQUIPMENT = REGISTRATE.item("component/ulv/research_equipment", Item::new)
                .model(ModelGen.basicItem(gregtech("items/metaitems/glass_vial/base"),
                        gregtech("items/metaitems/glass_vial/overlay")))
                .tint(0xFFFFFFFF, IRON.color)
                .register();

        COMPONENT_SETS = ComponentSet.builder()
                .components(Voltage.LV)
                .material(STEEL).heat(COPPER).pipe(BRONZE).rotor(TIN).magnetic(STEEL)
                .cable(TIN)
                .build()
                .components(Voltage.MV)
                .material(ALUMINIUM).heat(CUPRONICKEL).pipe(STEEL).rotor(BRONZE).magnetic(STEEL)
                .cable(COPPER)
                .build()
                // TODO
                .components(Voltage.HV)
                .material(ALUMINIUM).heat(CUPRONICKEL).pipe(STEEL).rotor(BRONZE).magnetic(STEEL)
                .cable(COPPER)
                .build()
                .components(Voltage.EV)
                // TODO
                .material(ALUMINIUM).heat(CUPRONICKEL).pipe(STEEL).rotor(BRONZE).magnetic(STEEL)
                .cable(COPPER)
                .build()
                .components(Voltage.IV)
                // TODO
                .material(ALUMINIUM).heat(CUPRONICKEL).pipe(STEEL).rotor(BRONZE).magnetic(STEEL)
                .cable(COPPER)
                .build()
                .buildObject();

        VACUUM_TUBE = REGISTRATE.item("circuit/vacuum_tube", Item::new)
                .model(ModelGen.basicItem(gregtech("items/metaitems/circuit.vacuum_tube")))
                .register();

        STEAM = REGISTRATE.simpleFluid("steam", gregtech("blocks/fluids/fluid.steam"));

        HEAT_PROOF_BLOCK = REGISTRATE.block("multi_block/solid/heat_proof", Block::new)
                .properties($ -> $.strength(2f, 8f))
                .blockState(ctx -> ctx.provider.simpleBlock(ctx.object,
                        ctx.provider.models().cubeAll(ctx.id,
                                gregtech("blocks/casings/solid/machine_casing_heatproof"))))
                .tag(AllTags.MINEABLE_WITH_WRENCH)
                .defaultBlockItem().dropSelf()
                .register();
    }

    public static ComponentSet componentSet(Voltage voltage) {
        return COMPONENT_SETS.get(voltage);
    }

    public static RegistryEntry<Item> researchEquipment(Voltage voltage) {
        return voltage == Voltage.ULV ? ULV_RESEARCH_EQUIPMENT :
                componentSet(voltage).researchEquipment;
    }

    public static void init() {}

    public static void initRecipes() {
        ulvRecipes();
        COMPONENT_SETS.values().forEach(ComponentSet::addRecipes);
        researchEquipments();

        for (var voltage : Voltage.between(Voltage.ULV, Voltage.HV)) {
            var consume = (int) voltage.value / 8 * (14 - voltage.rank);
            AllRecipes.STEAM_TURBINE.recipe(voltage.id)
                    .voltage(voltage)
                    .inputFluid(0, STEAM, consume)
                    .outputFluid(1, Fluids.WATER, (int) voltage.value / 8 * 5)
                    .build();
        }
    }

    private static void ulvRecipes() {
        REGISTRATE.vanillaRecipe(() -> ShapelessRecipeBuilder
                .shapeless(ULV_CABLE.get())
                .requires(Ingredient.of(IRON.tag("wire")), 4)
                .unlockedBy("has_wire", has(IRON.tag("wire"))));

        REGISTRATE.vanillaRecipe(() -> ShapedRecipeBuilder
                .shaped(VACUUM_TUBE.get())
                .pattern("BGB").pattern("WWW")
                .define('G', Items.GLASS)
                .define('W', COPPER.tag("wire"))
                .define('B', IRON.tag("bolt"))
                .unlockedBy("has_wire", has(COPPER.tag("wire"))));

        TOOL.recipe(ULV_MACHINE_HULL)
                .result(ULV_MACHINE_HULL, 1)
                .pattern("###").pattern("#W#").pattern("###")
                .define('#', IRON.tag("plate"))
                .define('W', ULV_CABLE)
                .toolTag(AllTags.TOOL_WRENCH)
                .build();

        ulvMachine(STONE_GENERATOR);
        ulvMachine(ORE_ANALYZER);
        ulvMachine(ORE_WASHER);
        ulvMachine(NETWORK_CONTROLLER.entry(), VACUUM_TUBE);
        ulvMachine(RESEARCH_TABLE.entry(Voltage.ULV), () -> Blocks.CRAFTING_TABLE);
        ulvMachine(ASSEMBLER.entry(Voltage.ULV), WORKBENCH.entry());
        ulvMachine(ELECTRIC_FURNACE.entry(Voltage.ULV), () -> Blocks.FURNACE);

        TOOL.recipe(ALLOY_SMELTER.entry(Voltage.ULV))
                .result(ALLOY_SMELTER.entry(Voltage.ULV), 1)
                .pattern("WVW").pattern("VHV").pattern("WVW")
                .define('W', ULV_CABLE)
                .define('H', ELECTRIC_FURNACE.entry(Voltage.ULV))
                .define('V', VACUUM_TUBE)
                .toolTag(AllTags.TOOL_WRENCH)
                .build();

        TOOL.recipe(STEAM_TURBINE.entry(Voltage.ULV))
                .result(STEAM_TURBINE.entry(Voltage.ULV), 1)
                .pattern("PVP").pattern("RHR").pattern("WVW")
                .define('P', COPPER.tag("pipe"))
                .define('R', IRON.tag("rotor"))
                .define('W', ULV_CABLE)
                .define('H', ULV_MACHINE_HULL)
                .define('V', VACUUM_TUBE)
                .toolTag(AllTags.TOOL_WRENCH)
                .build();

        TOOL.recipe(LOW_PRESSURE_BOILER.entry())
                .result(LOW_PRESSURE_BOILER.entry(), 1)
                .pattern("PPP").pattern("PWP").pattern("VFV")
                .define('P', IRON.tag("plate"))
                .define('W', ULV_CABLE)
                .define('V', VACUUM_TUBE)
                .define('F', Blocks.FURNACE.asItem())
                .toolTag(AllTags.TOOL_WRENCH)
                .build();
    }

    private static void ulvMachine(RegistryEntry<? extends ItemLike> result,
                                   Supplier<? extends ItemLike> base) {
        TOOL.recipe(result)
                .result(result, 1)
                .pattern("BBB").pattern("VHV").pattern("WVW")
                .define('B', base)
                .define('W', ULV_CABLE)
                .define('H', ULV_MACHINE_HULL)
                .define('V', VACUUM_TUBE)
                .toolTag(AllTags.TOOL_WRENCH)
                .build();
    }

    private static void ulvMachine(MachineSet set) {
        ulvMachine(set.entry(Voltage.ULV), set.entry(Voltage.PRIMITIVE));
    }

    private static void researchEquipments() {
        var workTicks = 200;

        var item = ULV_RESEARCH_EQUIPMENT;
        AllRecipes.ASSEMBLER.recipe(item)
                .outputItem(2, item, 1)
                .inputItem(0, IRON.tag("plate"), 1)
                .inputItem(0, COPPER.tag("wire"), 1)
                .workTicks(workTicks)
                .voltage(Voltage.ULV)
                .build();
    }
}

