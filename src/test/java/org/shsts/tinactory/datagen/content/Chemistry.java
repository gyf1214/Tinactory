package org.shsts.tinactory.datagen.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import static org.shsts.tinactory.content.AllItems.FERTILIZER;
import static org.shsts.tinactory.content.AllMaterials.AMMONIA;
import static org.shsts.tinactory.content.AllMaterials.AMMONIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.ANNEALED_COPPER;
import static org.shsts.tinactory.content.AllMaterials.BENZENE;
import static org.shsts.tinactory.content.AllMaterials.BIOMASS;
import static org.shsts.tinactory.content.AllMaterials.CALCIUM;
import static org.shsts.tinactory.content.AllMaterials.CALCIUM_CARBONATE;
import static org.shsts.tinactory.content.AllMaterials.CALCIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.CARBON;
import static org.shsts.tinactory.content.AllMaterials.CHARCOAL;
import static org.shsts.tinactory.content.AllMaterials.CHLORINE;
import static org.shsts.tinactory.content.AllMaterials.COAL;
import static org.shsts.tinactory.content.AllMaterials.COKE;
import static org.shsts.tinactory.content.AllMaterials.COPPER;
import static org.shsts.tinactory.content.AllMaterials.CREOSOTE_OIL;
import static org.shsts.tinactory.content.AllMaterials.ETHANE;
import static org.shsts.tinactory.content.AllMaterials.ETHYLENE;
import static org.shsts.tinactory.content.AllMaterials.HEAVY_FUEL;
import static org.shsts.tinactory.content.AllMaterials.HEAVY_OIL;
import static org.shsts.tinactory.content.AllMaterials.HYDROGEN;
import static org.shsts.tinactory.content.AllMaterials.HYDROGEN_SULFIDE;
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllMaterials.LIGHT_FUEL;
import static org.shsts.tinactory.content.AllMaterials.LIGHT_OIL;
import static org.shsts.tinactory.content.AllMaterials.LITHIUM;
import static org.shsts.tinactory.content.AllMaterials.LITHIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.MAGNESIUM;
import static org.shsts.tinactory.content.AllMaterials.MAGNESIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.METHANE;
import static org.shsts.tinactory.content.AllMaterials.NAPHTHA;
import static org.shsts.tinactory.content.AllMaterials.NATURAL_GAS;
import static org.shsts.tinactory.content.AllMaterials.NITROGEN;
import static org.shsts.tinactory.content.AllMaterials.OXYGEN;
import static org.shsts.tinactory.content.AllMaterials.PHENOL;
import static org.shsts.tinactory.content.AllMaterials.POTASSIUM;
import static org.shsts.tinactory.content.AllMaterials.POTASSIUM_CARBONATE;
import static org.shsts.tinactory.content.AllMaterials.POTASSIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.POTASSIUM_NITRATE;
import static org.shsts.tinactory.content.AllMaterials.PROPANE;
import static org.shsts.tinactory.content.AllMaterials.PROPENE;
import static org.shsts.tinactory.content.AllMaterials.REFINERY_GAS;
import static org.shsts.tinactory.content.AllMaterials.SODIUM;
import static org.shsts.tinactory.content.AllMaterials.SODIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.STONE;
import static org.shsts.tinactory.content.AllMaterials.TITANIUM;
import static org.shsts.tinactory.content.AllMaterials.TITANIUM_TETRACHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.TOLUENE;
import static org.shsts.tinactory.content.AllMaterials.WATER;
import static org.shsts.tinactory.content.AllMaterials.WROUGHT_IRON;
import static org.shsts.tinactory.content.AllRecipes.ARC_FURNACE;
import static org.shsts.tinactory.content.AllRecipes.BLAST_FURNACE;
import static org.shsts.tinactory.content.AllRecipes.CHEMICAL_REACTOR;
import static org.shsts.tinactory.content.AllRecipes.MIXER;
import static org.shsts.tinactory.content.AllRecipes.PYROLYSE_OVEN;
import static org.shsts.tinactory.core.util.LocHelper.suffix;
import static org.shsts.tinactory.test.TinactoryTest.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Chemistry {
    public static void init() {
        inorganic();
        oil();
        organic();
    }

    private static void inorganic() {
        ELECTROLYZER
            .voltage(Voltage.HV)
            .recipe(SODIUM_CHLORIDE, 1, 400, SODIUM, 1, CHLORINE, 0.5f)
            .recipe(POTASSIUM_CHLORIDE, 1, 480, POTASSIUM, 1, CHLORINE, 0.5f)
            .recipe(MAGNESIUM_CHLORIDE, 1, 320, MAGNESIUM, 1, CHLORINE, 1f)
            .recipe(CALCIUM_CHLORIDE, 1, 320, CALCIUM, 1, CHLORINE, 1f)
            .recipe(LITHIUM_CHLORIDE, 1, 400, LITHIUM, 1, CHLORINE, 0.5f);

        BLAST_FURNACE.recipe(DATA_GEN, suffix(TITANIUM.loc("ingot_hot"), "_from_titanium_tetrachloride"))
            .inputItem(MAGNESIUM.tag("dust"), 2)
            .inputFluid(TITANIUM_TETRACHLORIDE.fluid(), TITANIUM_TETRACHLORIDE.fluidAmount(1f))
            .outputItem(TITANIUM.entry("ingot_hot"), 1)
            .outputItem(MAGNESIUM_CHLORIDE.entry("dust"), 2)
            .voltage(Voltage.HV)
            .workTicks(800)
            .temperature(2300)
            .build();

        ARC_FURNACE.recipe(DATA_GEN, WROUGHT_IRON.loc("ingot"))
            .inputItem(IRON.tag("ingot"), 1)
            .inputFluid(OXYGEN.fluid(), OXYGEN.fluidAmount(0.05f))
            .outputItem(WROUGHT_IRON.entry("ingot"), 1)
            .voltage(Voltage.HV)
            .workTicks(64)
            .build()
            .recipe(DATA_GEN, ANNEALED_COPPER.loc("ingot"))
            .inputItem(COPPER.tag("ingot"), 1)
            .inputFluid(OXYGEN.fluid(), OXYGEN.fluidAmount(0.075f))
            .outputItem(ANNEALED_COPPER.entry("ingot"), 1)
            .voltage(Voltage.HV)
            .workTicks(96)
            .build();
    }

    private static void sulfuric(MaterialSet material, long workTicks) {
        CHEMICAL_REACTOR.recipe(DATA_GEN, material.fluidLoc())
            .input(material, "sulfuric", 1f)
            .input(HYDROGEN, 1f)
            .output(material, 1f)
            .output(HYDROGEN_SULFIDE, 1f)
            .workTicks(workTicks)
            .voltage(Voltage.MV)
            .requireTech(Technologies.OIL_PROCESSING)
            .build();
    }

    private static void oil() {
        DISTILLATION.voltage(Voltage.MV)
            .recipe(REFINERY_GAS, 1f, 192, METHANE, 0.4f, ETHANE, 0.6f, PROPANE, 0.2f, ETHYLENE, 0.1f)
            .recipe(REFINERY_GAS, "lightly_steam_cracked", 1f, 160,
                METHANE, 0.6f, ETHANE, 0.6f, PROPANE, 0.075f, ETHYLENE, 0.15f, PROPENE, 0.025f)
            .recipe(REFINERY_GAS, "severely_steam_cracked", 1f, 144,
                METHANE, 0.95f, ETHANE, 0.25f, PROPANE, 0.05f, ETHYLENE, 0.325f, PROPENE, 0.05f)
            .recipe(REFINERY_GAS, "lightly_hydro_cracked", 1f, 120,
                METHANE, 1.3f, ETHANE, 0.5f, ETHYLENE, 0.05f)
            .recipe(REFINERY_GAS, "severely_hydro_cracked", 1f, 100, METHANE, 2f, ETHANE, 0.2f)
            .recipe(NAPHTHA, 1f, 240, METHANE, 0.1f, ETHANE, 0.4f, PROPANE, 0.5f,
                ETHYLENE, 0.2f, PROPENE, 0.1f)
            .recipe(NAPHTHA, "lightly_steam_cracked", 1f, 208,
                METHANE, 0.2f, ETHANE, 0.45f, PROPANE, 0.3f, ETHYLENE, 0.4f, PROPENE, 0.1f)
            .recipe(NAPHTHA, "severely_steam_cracked", 1f, 192,
                METHANE, 0.375f, ETHANE, 0.125f, ETHYLENE, 0.9f, PROPENE, 0.225f)
            .recipe(NAPHTHA, "lightly_hydro_cracked", 1f, 160,
                METHANE, 0.9f, ETHANE, 0.45f, PROPANE, 0.3f, ETHYLENE, 0.2f)
            .recipe(NAPHTHA, "severely_hydro_cracked", 1f, 144,
                METHANE, 2.35f, ETHANE, 0.625f, PROPANE, 0.1f, ETHYLENE, 0.1f)
            .recipe(LIGHT_FUEL, "lightly_steam_cracked", 1f, 288,
                PROPANE, 0.4f, PROPENE, 0.525f, NAPHTHA, 0.475f)
            .recipe(LIGHT_FUEL, "severely_steam_cracked", 1f, 256,
                ETHYLENE, 0.65f, PROPENE, 0.65f, NAPHTHA, 0.2f)
            .recipe(LIGHT_FUEL, "lightly_hydro_cracked", 1f, 224,
                ETHANE, 1.05f, PROPANE, 0.125f, ETHYLENE, 0.15f, NAPHTHA, 0.5f)
            .recipe(LIGHT_FUEL, "severely_hydro_cracked", 1f, 192,
                METHANE, 0.5f, ETHANE, 1.2f, NAPHTHA, 0.5f)
            .recipe(HEAVY_FUEL, "lightly_hydro_cracked", 1f, 304,
                PROPANE, 0.25f, NAPHTHA, 0.4f, LIGHT_FUEL, 1.25f)
            .recipe(HEAVY_FUEL, "severely_hydro_cracked", 1f, 256,
                ETHANE, 0.55f, PROPANE, 0.65f, NAPHTHA, 0.8f, LIGHT_FUEL, 0.1f)
            .recipe(NATURAL_GAS, 1f, 192, REFINERY_GAS, "sulfuric", 1.5f, NAPHTHA, "sulfuric", 0.5f)
            .recipe(LIGHT_OIL, 1f, 240, REFINERY_GAS, "sulfuric", 0.6f, NAPHTHA, "sulfuric", 0.8f,
                LIGHT_FUEL, "sulfuric", 0.5f, HEAVY_FUEL, "sulfuric", 0.1f)
            .recipe(HEAVY_OIL, 1f, 360, REFINERY_GAS, "sulfuric", 0.2f, NAPHTHA, "sulfuric", 0.2f,
                LIGHT_FUEL, "sulfuric", 0.4f, HEAVY_FUEL, "sulfuric", 1.2f);

        sulfuric(REFINERY_GAS, 192);
        sulfuric(NAPHTHA, 240);
        sulfuric(LIGHT_FUEL, 320);
        sulfuric(HEAVY_FUEL, 432);
    }

    private static void organic() {
        DATA_GEN.nullRecipe(Blocks.COARSE_DIRT.asItem());

        MIXER.recipe(DATA_GEN, Blocks.DIRT)
            .outputItem(() -> Blocks.DIRT, 1)
            .inputItem(STONE.tag("dust"), 1)
            .inputFluid(BIOMASS.fluid(), BIOMASS.fluidAmount(1f))
            .workTicks(160)
            .voltage(Voltage.LV)
            .build()
            .recipe(DATA_GEN, Blocks.GRASS_BLOCK)
            .outputItem(() -> Blocks.GRASS_BLOCK, 1)
            .inputItem(() -> Blocks.DIRT, 1)
            .inputFluid(BIOMASS.fluid(), BIOMASS.fluidAmount(1f))
            .workTicks(160)
            .voltage(Voltage.LV)
            .build()
            .recipe(DATA_GEN, Blocks.PODZOL)
            .outputItem(() -> Blocks.PODZOL, 1)
            .inputItem(() -> Blocks.DIRT, 1)
            .inputItem(CARBON.tag("dust"), 1)
            .workTicks(160)
            .voltage(Voltage.LV)
            .build()
            .recipe(DATA_GEN, Blocks.COARSE_DIRT)
            .outputItem(() -> Blocks.COARSE_DIRT, 2)
            .inputItem(() -> Blocks.DIRT, 1)
            .inputItem(() -> Blocks.GRAVEL, 1)
            .workTicks(160)
            .voltage(Voltage.LV)
            .build()
            .recipe(DATA_GEN, Items.BONE_MEAL)
            .outputItem(() -> Items.BONE_MEAL, 1)
            .inputItem(CALCIUM_CARBONATE.tag("dust"), 1)
            .inputItem(POTASSIUM_CARBONATE.tag("dust"), 1)
            .workTicks(64)
            .voltage(Voltage.MV)
            .build()
            .recipe(DATA_GEN, FERTILIZER)
            .outputItem(FERTILIZER, 2)
            .inputItem(() -> Items.BONE_MEAL, 1)
            .inputItem(AMMONIUM_CHLORIDE.tag("dust"), 1)
            .inputItem(POTASSIUM_NITRATE.tag("dust"), 1)
            .workTicks(128)
            .voltage(Voltage.MV)
            .build();

        DISTILLATION.voltage(Voltage.MV)
            .recipe(CREOSOTE_OIL, 4f, 1200, CARBON, 1, AMMONIA, 1.2f, BENZENE, 1.4f,
                TOLUENE, 0.3f, PHENOL, 0.3f);

        AllRecipes.DISTILLATION
            .recipe(DATA_GEN, BIOMASS.fluidLoc())
            .inputFluid(BIOMASS.fluid(), BIOMASS.fluidAmount(1f))
            .outputItem(() -> Items.BONE_MEAL, 1)
            .outputFluid(METHANE.fluid(), METHANE.fluidAmount(0.6f))
            .outputFluid(AMMONIA.fluid(), AMMONIA.fluidAmount(0.3f))
            .outputFluid(WATER.fluid("gas"), WATER.fluidAmount(0.3f))
            .voltage(Voltage.MV)
            .workTicks(96)
            .build();

        PYROLYSE_OVEN.recipe(DATA_GEN, COKE.loc("primary"))
            .inputItem(COAL.tag("primary"), 16)
            .outputItem(COKE.entry("primary"), 16)
            .outputFluid(CREOSOTE_OIL.fluid(), CREOSOTE_OIL.fluidAmount(8f))
            .voltage(Voltage.LV)
            .workTicks(1280)
            .build()
            .recipe(DATA_GEN, suffix(COKE.loc("primary"), "_with_nitrogen"))
            .inputItem(COAL.tag("primary"), 16)
            .inputFluid(NITROGEN.fluid(), NITROGEN.fluidAmount(4f))
            .outputItem(COKE.entry("primary"), 16)
            .outputFluid(CREOSOTE_OIL.fluid(), CREOSOTE_OIL.fluidAmount(8f))
            .voltage(Voltage.LV)
            .workTicks(320)
            .build()
            .recipe(DATA_GEN, CHARCOAL.loc("primary"))
            .inputItem(ItemTags.LOGS_THAT_BURN, 16)
            .outputItem(CHARCOAL.entry("primary"), 16)
            .outputFluid(CREOSOTE_OIL.fluid(), CREOSOTE_OIL.fluidAmount(4f))
            .voltage(Voltage.LV)
            .workTicks(1280)
            .build()
            .recipe(DATA_GEN, suffix(CHARCOAL.loc("primary"), "_with_nitrogen"))
            .inputItem(ItemTags.LOGS_THAT_BURN, 16)
            .inputFluid(NITROGEN.fluid(), NITROGEN.fluidAmount(4f))
            .outputItem(CHARCOAL.entry("primary"), 16)
            .outputFluid(CREOSOTE_OIL.fluid(), CREOSOTE_OIL.fluidAmount(4f))
            .voltage(Voltage.LV)
            .workTicks(320)
            .build();
    }

    private static class DecomposeFactory {
        private final IRecipeType<ProcessingRecipe.Builder> recipeType;
        private Voltage voltage;

        private DecomposeFactory(IRecipeType<ProcessingRecipe.Builder> recipeType) {
            this.recipeType = recipeType;
        }

        public DecomposeFactory voltage(Voltage value) {
            voltage = value;
            return this;
        }

        public DecomposeFactory recipe(MaterialSet input, String sub, Number inputAmount, long workTicks,
            Object... components) {
            var isFluid = input.hasFluid(sub);
            var loc = isFluid ? input.fluidLoc(sub) : input.loc(sub);
            var builder = recipeType.recipe(DATA_GEN, loc)
                .voltage(voltage)
                .workTicks(workTicks);
            if (isFluid) {
                builder.inputFluid(input.fluid(sub), input.fluidAmount(sub, inputAmount.floatValue()));
            } else {
                builder.inputItem(input.tag(sub), inputAmount.intValue());
            }
            var i = 0;
            while (i < components.length) {
                var output = (MaterialSet) components[i++];
                var sub1 = output.hasItem("dust") ? "dust" : "fluid";
                if (i < components.length && components[i] instanceof String s) {
                    sub1 = s;
                    i++;
                }
                var outputAmount = (Number) components[i++];
                var isFluid1 = output.hasFluid(sub1);
                if (isFluid1) {
                    builder.outputFluid(output.fluid(sub1), output.fluidAmount(sub1,
                        outputAmount.floatValue()));
                } else {
                    if (outputAmount.doubleValue() < 1d) {
                        builder.outputItem(output.entry(sub1), 1, outputAmount.doubleValue());
                    } else {
                        builder.outputItem(output.entry(sub1), outputAmount.intValue());
                    }
                }
            }
            builder.build();
            return this;
        }

        public DecomposeFactory recipe(MaterialSet input, Number inputAmounts, long workTicks,
            Object... components) {
            return recipe(input, input.hasItem("dust") ? "dust" : "fluid", inputAmounts, workTicks, components);
        }
    }

    private static final DecomposeFactory DISTILLATION = new DecomposeFactory(AllRecipes.DISTILLATION);

    private static final DecomposeFactory ELECTROLYZER = new DecomposeFactory(AllRecipes.ELECTROLYZER);
}
