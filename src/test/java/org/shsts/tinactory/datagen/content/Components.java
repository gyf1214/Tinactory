package org.shsts.tinactory.datagen.content;


import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.material.ComponentSet;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.content.AllItems.COMPONENT_SETS;
import static org.shsts.tinactory.content.AllItems.HEAT_PROOF_BLOCK;
import static org.shsts.tinactory.content.AllItems.ULV_CABLE;
import static org.shsts.tinactory.content.AllItems.ULV_MACHINE_HULL;
import static org.shsts.tinactory.content.AllItems.ULV_RESEARCH_EQUIPMENT;
import static org.shsts.tinactory.content.AllItems.VACUUM_TUBE;
import static org.shsts.tinactory.content.AllMaterials.COPPER;
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllRecipes.ASSEMBLER;
import static org.shsts.tinactory.content.AllRecipes.TOOL_CRAFTING;
import static org.shsts.tinactory.content.AllRecipes.has;
import static org.shsts.tinactory.content.AllTags.MINEABLE_WITH_CUTTER;
import static org.shsts.tinactory.content.AllTags.MINEABLE_WITH_WRENCH;
import static org.shsts.tinactory.datagen.DataGen.DATA_GEN;
import static org.shsts.tinactory.datagen.content.Models.basicItem;
import static org.shsts.tinactory.datagen.content.Models.machineItem;
import static org.shsts.tinactory.datagen.content.Models.solidBlock;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Components {
    private static final String MACHINE_HULL_TEX = "overlay/machine/overlay_energy_out";
    private static final String RESEARCH_TEX = "metaitems/glass_vial/";

    public static void init() {
        componentItems();
        ulv();
        misc();
    }

    private static void componentItems() {
        DATA_GEN.block(ULV_CABLE)
                .blockState(Models::ulvCableBlock)
                .itemModel(Models::ulvCableItem)
                .tag(MINEABLE_WITH_CUTTER)
                .build()
                .item(ULV_MACHINE_HULL)
                .model(machineItem(Voltage.ULV, MACHINE_HULL_TEX))
                .build()
                .item(ULV_RESEARCH_EQUIPMENT)
                .model(basicItem(RESEARCH_TEX + "base", RESEARCH_TEX + "overlay"))
                .build()
                .item(VACUUM_TUBE)
                .model(basicItem("metaitems/circuit.vacuum_tube"))
                .build();

        for (var entry : COMPONENT_SETS.entrySet()) {
            componentItem(entry.getKey(), entry.getValue());
        }
    }

    private static void ulv() {
        DATA_GEN.vanillaRecipe(() -> ShapelessRecipeBuilder
                        .shapeless(ULV_CABLE.get())
                        .requires(Ingredient.of(IRON.tag("wire")), 4)
                        .unlockedBy("has_wire", has(IRON.tag("wire"))))
                .vanillaRecipe(() -> ShapedRecipeBuilder
                        .shaped(VACUUM_TUBE.get())
                        .pattern("BGB").pattern("WWW")
                        .define('G', Items.GLASS)
                        .define('W', COPPER.tag("wire"))
                        .define('B', IRON.tag("bolt"))
                        .unlockedBy("has_wire", has(COPPER.tag("wire"))));

        TOOL_CRAFTING.recipe(DATA_GEN, ULV_MACHINE_HULL)
                .result(ULV_MACHINE_HULL, 1)
                .pattern("###").pattern("#W#").pattern("###")
                .define('#', IRON.tag("plate"))
                .define('W', ULV_CABLE)
                .toolTag(AllTags.TOOL_WRENCH)
                .build();

        ASSEMBLER.recipe(DATA_GEN, ULV_RESEARCH_EQUIPMENT)
                .outputItem(2, ULV_RESEARCH_EQUIPMENT, 1)
                .inputItem(0, IRON.tag("plate"), 1)
                .inputItem(0, COPPER.tag("wire"), 1)
                .workTicks(200)
                .voltage(Voltage.ULV)
                .build();
    }

    private static void misc() {
        DATA_GEN.block(HEAT_PROOF_BLOCK)
                .blockState(solidBlock("casings/solid/machine_casing_heatproof"))
                .tag(MINEABLE_WITH_WRENCH)
                .build();
    }

    private static void componentItem(Voltage voltage, ComponentSet set) {
        for (var entry : set.dummyItems) {
            var names = entry.id.split("/");
            var name = names[names.length - 1];

            var tex = "metaitems/" + name.replace('_', '.') + "." + voltage.id;
            DATA_GEN.item(entry)
                    .model(basicItem(tex))
                    .build();
        }

        DATA_GEN.item(set.machineHull)
                .model(machineItem(voltage, MACHINE_HULL_TEX))
                .build()
                .block(set.cable)
                .blockState(Models::cableBlock)
                .itemModel(Models::cableItem)
                .tag(MINEABLE_WITH_CUTTER)
                .build()
                .item(set.researchEquipment)
                .model(basicItem(RESEARCH_TEX + "base", RESEARCH_TEX + "overlay"))
                .build();
    }
}
