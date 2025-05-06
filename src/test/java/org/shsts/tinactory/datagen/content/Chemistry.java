package org.shsts.tinactory.datagen.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import static org.shsts.tinactory.content.AllItems.BIOMASS;
import static org.shsts.tinactory.content.AllMaterials.AIR;
import static org.shsts.tinactory.content.AllMaterials.ALUMINIUM;
import static org.shsts.tinactory.content.AllMaterials.ARGON;
import static org.shsts.tinactory.content.AllMaterials.BAUXITE;
import static org.shsts.tinactory.content.AllMaterials.CALCIUM_CARBONATE;
import static org.shsts.tinactory.content.AllMaterials.CALCIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.CALCIUM_HYDROXIDE;
import static org.shsts.tinactory.content.AllMaterials.CARBON;
import static org.shsts.tinactory.content.AllMaterials.CARBON_DIOXIDE;
import static org.shsts.tinactory.content.AllMaterials.CHLORINE;
import static org.shsts.tinactory.content.AllMaterials.ETHANE;
import static org.shsts.tinactory.content.AllMaterials.ETHANOL;
import static org.shsts.tinactory.content.AllMaterials.ETHYLENE;
import static org.shsts.tinactory.content.AllMaterials.HEAVY_FUEL;
import static org.shsts.tinactory.content.AllMaterials.HEAVY_OIL;
import static org.shsts.tinactory.content.AllMaterials.HYDROGEN;
import static org.shsts.tinactory.content.AllMaterials.HYDROGEN_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.HYDROGEN_SULFIDE;
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllMaterials.IRON_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.LIGHT_FUEL;
import static org.shsts.tinactory.content.AllMaterials.LIGHT_OIL;
import static org.shsts.tinactory.content.AllMaterials.LITHIUM_BRINE;
import static org.shsts.tinactory.content.AllMaterials.MAGNESIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.METHANE;
import static org.shsts.tinactory.content.AllMaterials.NAPHTHA;
import static org.shsts.tinactory.content.AllMaterials.NATURAL_GAS;
import static org.shsts.tinactory.content.AllMaterials.NITRIC_ACID;
import static org.shsts.tinactory.content.AllMaterials.NITROGEN;
import static org.shsts.tinactory.content.AllMaterials.OXYGEN;
import static org.shsts.tinactory.content.AllMaterials.PE;
import static org.shsts.tinactory.content.AllMaterials.POTASSIUM_CARBONATE;
import static org.shsts.tinactory.content.AllMaterials.POTASSIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.POTASSIUM_NITRATE;
import static org.shsts.tinactory.content.AllMaterials.PROPANE;
import static org.shsts.tinactory.content.AllMaterials.PROPENE;
import static org.shsts.tinactory.content.AllMaterials.PVC;
import static org.shsts.tinactory.content.AllMaterials.REFINERY_GAS;
import static org.shsts.tinactory.content.AllMaterials.RUTILE;
import static org.shsts.tinactory.content.AllMaterials.SALT_WATER;
import static org.shsts.tinactory.content.AllMaterials.SEA_WATER;
import static org.shsts.tinactory.content.AllMaterials.SODIUM_CARBONATE;
import static org.shsts.tinactory.content.AllMaterials.SODIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.SODIUM_HYDROXIDE;
import static org.shsts.tinactory.content.AllMaterials.STEAM;
import static org.shsts.tinactory.content.AllMaterials.STONE;
import static org.shsts.tinactory.content.AllMaterials.SULFUR;
import static org.shsts.tinactory.content.AllMaterials.SULFURIC_ACID;
import static org.shsts.tinactory.content.AllMaterials.VINYL_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.WATER;
import static org.shsts.tinactory.content.AllRecipes.CHEMICAL_REACTOR;
import static org.shsts.tinactory.content.AllRecipes.MIXER;
import static org.shsts.tinactory.content.AllRecipes.STONE_GENERATOR;
import static org.shsts.tinactory.content.AllRecipes.VACUUM_FREEZER;
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
        STONE_GENERATOR.recipe(DATA_GEN, AIR.fluidLoc())
            .outputFluid(AIR.fluid(), AIR.fluidAmount(1))
            .voltage(Voltage.MV)
            .build()
            .recipe(DATA_GEN, SEA_WATER.fluidLoc())
            .outputFluid(SEA_WATER.fluid(), SEA_WATER.fluidAmount(1))
            .voltage(Voltage.MV)
            .build();

        VACUUM_FREEZER.recipe(DATA_GEN, AIR.fluidLoc("liquid"))
            .inputFluid(AIR.fluid(), AIR.fluidAmount(1))
            .outputFluid(AIR.fluid("liquid"), AIR.fluidAmount("liquid", 1))
            .workTicks(200)
            .voltage(Voltage.MV)
            .build()
            .recipe(DATA_GEN, Fluids.WATER)
            .inputFluid(STEAM, 1000)
            .outputFluid(() -> Fluids.WATER, 1000)
            .workTicks(32)
            .voltage(Voltage.MV)
            .build();

        DISTILLATION.voltage(Voltage.MV)
            .recipe(AIR, "liquid", 1f, 100, NITROGEN, 0.78f, OXYGEN, 0.21f, ARGON, 0.01f)
            .recipe(SEA_WATER, 10f, 4000,
                SODIUM_CHLORIDE, 5, POTASSIUM_CHLORIDE, 1, MAGNESIUM_CHLORIDE, 0.5,
                CALCIUM_CHLORIDE, 0.2, WATER, "gas", 6.4f, LITHIUM_BRINE, 0.1f)
            .recipe(SALT_WATER, 2f, 800, SODIUM_CHLORIDE, 1, WATER, "gas", 1f)
            .recipe(SULFURIC_ACID, "dilute", 2f, 800, SULFURIC_ACID, 1f, WATER, "gas", 1f)
            .recipe(WATER, 1f, 400, WATER, "gas", 1f);

        ELECTROLYZER.voltage(Voltage.MV)
            .recipe(WATER, 1f, 1600, HYDROGEN, 1f, OXYGEN, 0.5f)
            .recipe(SALT_WATER, 2f, 1600, HYDROGEN, 0.5f, CHLORINE, 0.5f, SODIUM_HYDROXIDE, 1)
            .recipe(SEA_WATER, 2f, 3200, HYDROGEN, 0.5f, CHLORINE, 0.5f, SODIUM_HYDROXIDE, 1)
            .recipe(BAUXITE, 15, 320, ALUMINIUM, "dust", 6, OXYGEN, 9f, RUTILE, 1);

        CHEMICAL_REACTOR.recipe(DATA_GEN, HYDROGEN_CHLORIDE.fluidLoc())
            .input(HYDROGEN, 0.5f)
            .input(CHLORINE, 0.5f)
            .output(HYDROGEN_CHLORIDE)
            .workTicks(64)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CHEMISTRY)
            .build()
            .recipe(DATA_GEN, CARBON_DIOXIDE.fluidLoc())
            .input(CARBON)
            .input(OXYGEN)
            .output(CARBON_DIOXIDE)
            .workTicks(240)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CHEMISTRY)
            .build()
            .recipe(DATA_GEN, CALCIUM_CARBONATE.loc("dust"))
            .input(SODIUM_CARBONATE)
            .input(CALCIUM_CHLORIDE)
            .output(CALCIUM_CARBONATE)
            .output(SODIUM_CHLORIDE, 2f)
            .workTicks(64)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CHEMISTRY)
            .build()
            .recipe(DATA_GEN, suffix(SODIUM_CHLORIDE.loc("dust"), "_from_carbonate"))
            .input(SODIUM_CARBONATE)
            .input(HYDROGEN_CHLORIDE, 2f)
            .output(SODIUM_CHLORIDE, 2f)
            .output(WATER)
            .output(CARBON_DIOXIDE)
            .workTicks(160)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CHEMISTRY)
            .build()
            .recipe(DATA_GEN, SODIUM_CARBONATE.loc("dust"))
            .input(SODIUM_HYDROXIDE, 2f)
            .input(CARBON_DIOXIDE)
            .output(SODIUM_CARBONATE)
            .output(WATER)
            .workTicks(128)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CHEMISTRY)
            .build()
            .recipe(DATA_GEN, SODIUM_CHLORIDE.loc("dust"))
            .input(SODIUM_HYDROXIDE)
            .input(HYDROGEN_CHLORIDE)
            .output(SODIUM_CHLORIDE)
            .output(WATER)
            .workTicks(32)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CHEMISTRY)
            .build()
            .recipe(DATA_GEN, CALCIUM_HYDROXIDE.loc("dust"))
            .input(CALCIUM_CARBONATE)
            .inputFluid(STEAM, 1000)
            .output(CALCIUM_HYDROXIDE)
            .output(CARBON_DIOXIDE)
            .workTicks(400)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CHEMISTRY)
            .build()
            .recipe(DATA_GEN, suffix(CALCIUM_CHLORIDE.loc("dust"), "_from_carbonate"))
            .input(CALCIUM_CARBONATE)
            .input(HYDROGEN_CHLORIDE, 2f)
            .output(CALCIUM_CHLORIDE)
            .output(WATER)
            .output(CARBON_DIOXIDE)
            .workTicks(160)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CHEMISTRY)
            .build()
            .recipe(DATA_GEN, CALCIUM_CHLORIDE.loc("dust"))
            .input(CALCIUM_HYDROXIDE)
            .input(HYDROGEN_CHLORIDE, 2f)
            .output(CALCIUM_CHLORIDE)
            .output(WATER, 2f)
            .workTicks(32)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CHEMISTRY)
            .build()
            .recipe(DATA_GEN, SODIUM_HYDROXIDE.loc("dust"))
            .input(CALCIUM_HYDROXIDE)
            .input(SODIUM_CARBONATE)
            .output(SODIUM_HYDROXIDE, 2f)
            .output(CALCIUM_CARBONATE)
            .workTicks(128)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CHEMISTRY)
            .build()
            .recipe(DATA_GEN, SULFURIC_ACID.fluidLoc("gas"))
            .input(HYDROGEN_SULFIDE)
            .input(OXYGEN, 2f)
            .output(SULFURIC_ACID, "gas", 1f)
            .output(WATER)
            .workTicks(320)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CHEMISTRY)
            .build()
            .recipe(DATA_GEN, suffix(SULFURIC_ACID.fluidLoc("gas"), "_from_sulfur"))
            .input(SULFUR)
            .input(OXYGEN, 1.5f)
            .output(SULFURIC_ACID, "gas", 1f)
            .workTicks(480)
            .build()
            .recipe(DATA_GEN, SULFURIC_ACID.fluidLoc("dilute"))
            .input(SULFURIC_ACID, "gas", 1f)
            .input(WATER, 2f)
            .output(SULFURIC_ACID, "dilute", 2f)
            .workTicks(64)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CHEMISTRY)
            .build()
            .recipe(DATA_GEN, POTASSIUM_CARBONATE.loc("dust"))
            .input(POTASSIUM_CHLORIDE, 2f)
            .input(SODIUM_CARBONATE)
            .output(POTASSIUM_CARBONATE)
            .output(SODIUM_CHLORIDE, 2f)
            .workTicks(128)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CHEMISTRY)
            .build()
            .recipe(DATA_GEN, POTASSIUM_NITRATE.loc("dust"))
            .input(NITRIC_ACID, 2f)
            .input(POTASSIUM_CARBONATE)
            .output(POTASSIUM_NITRATE, 2f)
            .output(WATER)
            .output(CARBON_DIOXIDE)
            .workTicks(160)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CHEMISTRY)
            .build()
            .recipe(DATA_GEN, IRON_CHLORIDE.fluidLoc())
            .input(IRON)
            .input(HYDROGEN_CHLORIDE, 3f)
            .output(IRON_CHLORIDE)
            .output(HYDROGEN, 1.5f)
            .workTicks(160)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CHEMISTRY)
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
        MIXER.recipe(DATA_GEN, Blocks.DIRT)
            .inputItem(STONE.tag("dust"), 1)
            .inputFluid(BIOMASS, 1000)
            .outputItem(() -> Blocks.DIRT, 1)
            .workTicks(160)
            .voltage(Voltage.MV)
            .build();

        CHEMICAL_REACTOR.recipe(DATA_GEN, suffix(CARBON_DIOXIDE.fluidLoc(), "_from_methane"))
            .input(METHANE)
            .input(WATER, 2f)
            .output(CARBON_DIOXIDE)
            .output(HYDROGEN, 4f)
            .workTicks(128)
            .voltage(Voltage.MV)
            .requireTech(Technologies.ORGANIC_CHEMISTRY)
            .build()
            .recipe(DATA_GEN, PE.fluidLoc())
            .input(ETHYLENE, 0.144f)
            .input(OXYGEN)
            .output(PE)
            .workTicks(160)
            .voltage(Voltage.MV)
            .requireTech(Technologies.ORGANIC_CHEMISTRY)
            .build()
            .recipe(DATA_GEN, VINYL_CHLORIDE.fluidLoc())
            .input(ETHANE)
            .input(CHLORINE, 2f)
            .output(VINYL_CHLORIDE, 1f)
            .output(HYDROGEN_CHLORIDE, 3f)
            .workTicks(256)
            .voltage(Voltage.MV)
            .requireTech(Technologies.ORGANIC_CHEMISTRY)
            .build()
            .recipe(DATA_GEN, PVC.fluidLoc())
            .input(VINYL_CHLORIDE, 0.144f)
            .input(OXYGEN)
            .output(PVC)
            .workTicks(200)
            .voltage(Voltage.MV)
            .requireTech(Technologies.ORGANIC_CHEMISTRY)
            .build()
            .recipe(DATA_GEN, suffix(ETHYLENE.fluidLoc(), "_from_ethanol"))
            .input(ETHANOL)
            .input(SULFURIC_ACID)
            .output(ETHYLENE)
            .output(SULFURIC_ACID, "dilute", 2f)
            .workTicks(240)
            .voltage(Voltage.MV)
            .requireTech(Technologies.ORGANIC_CHEMISTRY)
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
                var sub1 = output.hasFluid() ? "fluid" : "dust";
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
            return recipe(input, input.hasFluid() ? "fluid" : "dust", inputAmounts, workTicks, components);
        }
    }

    private static final DecomposeFactory DISTILLATION = new DecomposeFactory(AllRecipes.DISTILLATION);

    private static final DecomposeFactory ELECTROLYZER = new DecomposeFactory(AllRecipes.ELECTROLYZER);
}
