package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.material.ComponentSet;
import org.shsts.tinactory.content.model.CableModel;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.content.network.CableBlock;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllMaterials.ALUMINIUM;
import static org.shsts.tinactory.content.AllMaterials.COPPER;
import static org.shsts.tinactory.content.AllMaterials.CUPRONICKEL;
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllMaterials.STEEL;
import static org.shsts.tinactory.content.AllMaterials.TIN;
import static org.shsts.tinactory.content.AllRecipes.has;
import static org.shsts.tinactory.content.model.ModelGen.gregtech;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllItems {
    public static final RegistryEntry<CableBlock> ULV_CABLE;
    public static final Map<Voltage, ComponentSet> COMPONENT_SETS;
    public static final RegistryEntry<Item> VACUUM_TUBE;

    static {
        ULV_CABLE = REGISTRATE.block("network/cable/ulv",
                        properties -> new CableBlock(properties, CableBlock.WIRE_RADIUS, Voltage.ULV, 2.0))
                .blockState(ctx -> CableModel.blockState(ctx, true))
                .tint(IRON.color)
                .tag(AllTags.MINEABLE_WITH_CUTTER)
                .defaultBlockItem(CableModel::ulvItemModel).dropSelf()
                .register();

        COMPONENT_SETS = ComponentSet.builder()
                .components(Voltage.LV)
                .material(STEEL, COPPER)
                .cable(TIN, 1.0)
                .build()
                .components(Voltage.MV)
                .material(ALUMINIUM, CUPRONICKEL)
                .cable(COPPER, 1.0)
                .build()
                .buildObject();

        VACUUM_TUBE = REGISTRATE.item("circuit/vacuum_tube", Item::new)
                .model(ModelGen.basicItem(gregtech("items/metaitems/circuit.vacuum_tube")))
                .register();
    }

    public static void init() {}

    public static void initRecipes() {
        ulvRecipes();
        COMPONENT_SETS.values().forEach(ComponentSet::addRecipes);
    }

    private static void ulvRecipes() {
        REGISTRATE.vanillaRecipe(() -> ShapelessRecipeBuilder
                .shapeless(ULV_CABLE.get())
                .requires(Ingredient.of(IRON.tag("wire")), 4)
                .unlockedBy("has_wire", has(IRON.tag("wire"))));

        REGISTRATE.vanillaRecipe(() -> ShapedRecipeBuilder
                .shaped(VACUUM_TUBE.get())
                .pattern(" G ").pattern("GWG").pattern("BBB")
                .define('G', Items.GLASS)
                .define('W', COPPER.tag("wire"))
                .define('B', IRON.tag("bolt"))
                .unlockedBy("has_wire", has(COPPER.tag("wire"))));
    }
}
