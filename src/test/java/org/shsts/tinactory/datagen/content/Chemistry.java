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
import static org.shsts.tinactory.content.AllMaterials.AIR;
import static org.shsts.tinactory.content.AllMaterials.ALUMINIUM;
import static org.shsts.tinactory.content.AllMaterials.ALUMINIUM_OXIDE;
import static org.shsts.tinactory.content.AllMaterials.AMMONIA;
import static org.shsts.tinactory.content.AllMaterials.AMMONIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.ANNEALED_COPPER;
import static org.shsts.tinactory.content.AllMaterials.ARGON;
import static org.shsts.tinactory.content.AllMaterials.BAUXITE;
import static org.shsts.tinactory.content.AllMaterials.BENZENE;
import static org.shsts.tinactory.content.AllMaterials.BIOMASS;
import static org.shsts.tinactory.content.AllMaterials.BLUE_TOPAZ;
import static org.shsts.tinactory.content.AllMaterials.CALCIUM;
import static org.shsts.tinactory.content.AllMaterials.CALCIUM_CARBONATE;
import static org.shsts.tinactory.content.AllMaterials.CALCIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.CALCIUM_HYDROXIDE;
import static org.shsts.tinactory.content.AllMaterials.CARBON;
import static org.shsts.tinactory.content.AllMaterials.CARBON_DIOXIDE;
import static org.shsts.tinactory.content.AllMaterials.CHARCOAL;
import static org.shsts.tinactory.content.AllMaterials.CHLORINE;
import static org.shsts.tinactory.content.AllMaterials.CHLOROFORM;
import static org.shsts.tinactory.content.AllMaterials.CHROME;
import static org.shsts.tinactory.content.AllMaterials.COAL;
import static org.shsts.tinactory.content.AllMaterials.COKE;
import static org.shsts.tinactory.content.AllMaterials.COPPER;
import static org.shsts.tinactory.content.AllMaterials.CREOSOTE_OIL;
import static org.shsts.tinactory.content.AllMaterials.ETHANE;
import static org.shsts.tinactory.content.AllMaterials.ETHANOL;
import static org.shsts.tinactory.content.AllMaterials.ETHYLENE;
import static org.shsts.tinactory.content.AllMaterials.GRAPHITE;
import static org.shsts.tinactory.content.AllMaterials.HEAVY_FUEL;
import static org.shsts.tinactory.content.AllMaterials.HEAVY_OIL;
import static org.shsts.tinactory.content.AllMaterials.HYDROGEN;
import static org.shsts.tinactory.content.AllMaterials.HYDROGEN_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.HYDROGEN_FLUORIDE;
import static org.shsts.tinactory.content.AllMaterials.HYDROGEN_SULFIDE;
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllMaterials.IRON_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.LIGHT_FUEL;
import static org.shsts.tinactory.content.AllMaterials.LIGHT_OIL;
import static org.shsts.tinactory.content.AllMaterials.LITHIUM;
import static org.shsts.tinactory.content.AllMaterials.LITHIUM_BRINE;
import static org.shsts.tinactory.content.AllMaterials.LITHIUM_CARBONATE;
import static org.shsts.tinactory.content.AllMaterials.LITHIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.MAGNESIUM;
import static org.shsts.tinactory.content.AllMaterials.MAGNESIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.METHANE;
import static org.shsts.tinactory.content.AllMaterials.NAPHTHA;
import static org.shsts.tinactory.content.AllMaterials.NATURAL_GAS;
import static org.shsts.tinactory.content.AllMaterials.NITRIC_ACID;
import static org.shsts.tinactory.content.AllMaterials.NITROGEN;
import static org.shsts.tinactory.content.AllMaterials.OXYGEN;
import static org.shsts.tinactory.content.AllMaterials.PE;
import static org.shsts.tinactory.content.AllMaterials.PHENOL;
import static org.shsts.tinactory.content.AllMaterials.POTASSIUM;
import static org.shsts.tinactory.content.AllMaterials.POTASSIUM_CARBONATE;
import static org.shsts.tinactory.content.AllMaterials.POTASSIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.POTASSIUM_NITRATE;
import static org.shsts.tinactory.content.AllMaterials.PROPANE;
import static org.shsts.tinactory.content.AllMaterials.PROPENE;
import static org.shsts.tinactory.content.AllMaterials.PTFE;
import static org.shsts.tinactory.content.AllMaterials.PVC;
import static org.shsts.tinactory.content.AllMaterials.RAW_RUBBER;
import static org.shsts.tinactory.content.AllMaterials.REFINERY_GAS;
import static org.shsts.tinactory.content.AllMaterials.RUBBER;
import static org.shsts.tinactory.content.AllMaterials.RUBY;
import static org.shsts.tinactory.content.AllMaterials.RUTILE;
import static org.shsts.tinactory.content.AllMaterials.SALT_WATER;
import static org.shsts.tinactory.content.AllMaterials.SAPPHIRE;
import static org.shsts.tinactory.content.AllMaterials.SEA_WATER;
import static org.shsts.tinactory.content.AllMaterials.SILICON;
import static org.shsts.tinactory.content.AllMaterials.SILICON_DIOXIDE;
import static org.shsts.tinactory.content.AllMaterials.SODIUM;
import static org.shsts.tinactory.content.AllMaterials.SODIUM_CARBONATE;
import static org.shsts.tinactory.content.AllMaterials.SODIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.SODIUM_HYDROXIDE;
import static org.shsts.tinactory.content.AllMaterials.SODIUM_SULFATE;
import static org.shsts.tinactory.content.AllMaterials.STONE;
import static org.shsts.tinactory.content.AllMaterials.SULFUR;
import static org.shsts.tinactory.content.AllMaterials.SULFURIC_ACID;
import static org.shsts.tinactory.content.AllMaterials.TETRA_FLUORO_ETHYLENE;
import static org.shsts.tinactory.content.AllMaterials.TITANIUM;
import static org.shsts.tinactory.content.AllMaterials.TITANIUM_TETRACHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.TOLUENE;
import static org.shsts.tinactory.content.AllMaterials.TOPAZ;
import static org.shsts.tinactory.content.AllMaterials.VINYL_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.WATER;
import static org.shsts.tinactory.content.AllMaterials.WROUGHT_IRON;
import static org.shsts.tinactory.content.AllRecipes.ARC_FURNACE;
import static org.shsts.tinactory.content.AllRecipes.BLAST_FURNACE;
import static org.shsts.tinactory.content.AllRecipes.CHEMICAL_REACTOR;
import static org.shsts.tinactory.content.AllRecipes.MIXER;
import static org.shsts.tinactory.content.AllRecipes.PYROLYSE_OVEN;
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
            .outputFluid(AIR.fluid(), AIR.fluidAmount(1f))
            .voltage(Voltage.MV)
            .build()
            .recipe(DATA_GEN, SEA_WATER.fluidLoc())
            .outputFluid(SEA_WATER.fluid(), SEA_WATER.fluidAmount(1f))
            .voltage(Voltage.MV)
            .build();

        VACUUM_FREEZER.recipe(DATA_GEN, AIR.fluidLoc("liquid"))
            .inputFluid(AIR.fluid(), AIR.fluidAmount(1f))
            .outputFluid(AIR.fluid("liquid"), AIR.fluidAmount("liquid", 1f))
            .workTicks(200)
            .voltage(Voltage.MV)
            .build()
            .recipe(DATA_GEN, WATER.fluidLoc())
            .inputFluid(WATER.fluid("gas"), WATER.fluidAmount("gas", 1f))
            .outputFluid(WATER.fluid(), WATER.fluidAmount(1f))
            .workTicks(32)
            .voltage(Voltage.MV)
            .build();

        DISTILLATION.voltage(Voltage.MV)
            .recipe(AIR, "liquid", 1f, 96, NITROGEN, 0.78f, OXYGEN, 0.21f, ARGON, 0.01f)
            .recipe(SEA_WATER, 10f, 2000,
                SODIUM_CHLORIDE, 5, POTASSIUM_CHLORIDE, 1, MAGNESIUM_CHLORIDE, 0.5,
                CALCIUM_CHLORIDE, 0.2, WATER, "gas", 6.4f, LITHIUM_BRINE, 0.1f)
            .recipe(SALT_WATER, 2f, 320, SODIUM_CHLORIDE, 1, WATER, "gas", 1f)
            .recipe(SULFURIC_ACID, "dilute", 2f, 320, SULFURIC_ACID, 1f, WATER, "gas", 1f)
            .recipe(WATER, 1f, 300, WATER, "gas", 1f);

        ELECTROLYZER.voltage(Voltage.MV)
            .recipe(WATER, 1f, 800, HYDROGEN, 1f, OXYGEN, 0.5f)
            .recipe(SALT_WATER, 2f, 400, HYDROGEN, 0.5f, CHLORINE, 0.5f, SODIUM_HYDROXIDE, 1)
            .recipe(SEA_WATER, 2f, 1600, HYDROGEN, 0.5f, CHLORINE, 0.5f, SODIUM_HYDROXIDE, 1)
            .recipe(BAUXITE, 6, 640, ALUMINIUM, 6, OXYGEN, 4.5f, RUTILE, 1)
            .recipe(CHARCOAL, 1, 64, CARBON, 1)
            .recipe(COAL, 1, 40, CARBON, 2)
            .recipe(COKE, 1, 32, CARBON, 2)
            .recipe(GRAPHITE, 1, 64, CARBON, 4)
            .recipe(SILICON_DIOXIDE, 1, 480, SILICON, 1, OXYGEN, 1f)
            .recipe(ALUMINIUM_OXIDE, 1, 96, ALUMINIUM, 1, OXYGEN, 0.75f)
            .voltage(Voltage.HV)
            .recipe(SODIUM_CHLORIDE, 1, 400, SODIUM, 1, CHLORINE, 0.5f)
            .recipe(POTASSIUM_CHLORIDE, 1, 480, POTASSIUM, 1, CHLORINE, 0.5f)
            .recipe(MAGNESIUM_CHLORIDE, 1, 320, MAGNESIUM, 1, CHLORINE, 1f)
            .recipe(CALCIUM_CHLORIDE, 1, 320, CALCIUM, 1, CHLORINE, 1f)
            .recipe(LITHIUM_CHLORIDE, 1, 400, LITHIUM, 1, CHLORINE, 0.5f);

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
            .recipe(DATA_GEN, suffix(SALT_WATER.fluidLoc(), "_from_carbonate"))
            .input(SODIUM_CARBONATE)
            .input(HYDROGEN_CHLORIDE, 2f)
            .input(WATER)
            .output(SALT_WATER, 4f)
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
            .recipe(DATA_GEN, SALT_WATER.fluidLoc())
            .input(SODIUM_HYDROXIDE)
            .input(HYDROGEN_CHLORIDE)
            .output(SALT_WATER, 2f)
            .workTicks(32)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CHEMISTRY)
            .build()
            .recipe(DATA_GEN, CALCIUM_HYDROXIDE.loc("dust"))
            .input(CALCIUM_CARBONATE)
            .input(WATER, "gas", 1f)
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
            .input(SULFUR)
            .input(OXYGEN, 1.5f)
            .output(SULFURIC_ACID, "gas", 1f)
            .workTicks(480)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CHEMISTRY)
            .build()
            .recipe(DATA_GEN, SULFURIC_ACID.fluidLoc())
            .input(SULFURIC_ACID, "gas", 1f)
            .input(WATER, 1f)
            .output(SULFURIC_ACID, 1f)
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
            .recipe(DATA_GEN, SODIUM_SULFATE.loc("dust"))
            .input(SULFURIC_ACID)
            .input(SODIUM_CHLORIDE, 2f)
            .output(SODIUM_SULFATE)
            .output(HYDROGEN_CHLORIDE, 2f)
            .workTicks(320)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CHEMISTRY)
            .build()
            .recipe(DATA_GEN, suffix(SODIUM_SULFATE.loc("dust"), "_from_carbonate"))
            .input(SODIUM_CARBONATE)
            .input(SULFURIC_ACID, "dilute", 2f)
            .output(SODIUM_SULFATE)
            .output(WATER, 2f)
            .output(CARBON_DIOXIDE)
            .workTicks(160)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CHEMISTRY)
            .build()
            .recipe(DATA_GEN, suffix(SODIUM_SULFATE.loc("dust"), "_from_hydroxide"))
            .input(SODIUM_HYDROXIDE, 2f)
            .input(SULFURIC_ACID, "dilute", 2f)
            .output(SODIUM_SULFATE)
            .output(WATER, 3f)
            .workTicks(64)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CHEMISTRY)
            .build()
            .recipe(DATA_GEN, SULFUR.loc("dust"))
            .input(SODIUM_SULFATE)
            .input(HYDROGEN_SULFIDE, 3f)
            .input(HYDROGEN_CHLORIDE, 2f)
            .output(SULFUR, 4f)
            .output(SALT_WATER, 4f)
            .workTicks(320)
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
            .build()
            .recipe(DATA_GEN, AMMONIUM_CHLORIDE.loc("dust"))
            .input(AMMONIA)
            .input(HYDROGEN_CHLORIDE)
            .output(AMMONIUM_CHLORIDE)
            .workTicks(64)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CHEMISTRY)
            .build()
            .recipe(DATA_GEN, suffix(AMMONIA.fluidLoc(), "_from_ammonium_chloride"))
            .input(AMMONIUM_CHLORIDE)
            .output(AMMONIA)
            .output(HYDROGEN_CHLORIDE)
            .workTicks(320)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CHEMISTRY)
            .build()
            .recipe(DATA_GEN, LITHIUM_CHLORIDE.loc("dust"))
            .input(LITHIUM_CARBONATE)
            .input(HYDROGEN_CHLORIDE, 2f)
            .output(LITHIUM_CHLORIDE, 2f)
            .output(WATER)
            .output(CARBON_DIOXIDE)
            .workTicks(160)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CHEMISTRY)
            .build();

        // HV
        CHEMICAL_REACTOR.recipe(DATA_GEN, RUBY.loc("dust"))
            .input(RUBY, "dust", 2f)
            .input(HYDROGEN_CHLORIDE, 2f)
            .input(SODIUM_HYDROXIDE, 2f)
            .output(ALUMINIUM_OXIDE, 2f)
            .output(SALT_WATER, 4f)
            .output(CHROME)
            .workTicks(320)
            .voltage(Voltage.HV)
            .requireTech(Technologies.HYDROMETALLURGY)
            .build()
            .recipe(DATA_GEN, SAPPHIRE.loc("dust"))
            .input(SAPPHIRE, "dust", 1f)
            .input(HYDROGEN_CHLORIDE)
            .input(SODIUM_HYDROXIDE)
            .output(ALUMINIUM_OXIDE)
            .output(SALT_WATER, 2f)
            .workTicks(160)
            .voltage(Voltage.HV)
            .requireTech(Technologies.HYDROMETALLURGY)
            .build()
            .recipe(DATA_GEN, TOPAZ.loc("dust"))
            .input(TOPAZ, "dust", 2f)
            .input(HYDROGEN_CHLORIDE, 6f)
            .input(SODIUM_HYDROXIDE, 6f)
            .output(ALUMINIUM_OXIDE, 2f)
            .output(SILICON_DIOXIDE)
            .output(SALT_WATER, 12f)
            .output(HYDROGEN_FLUORIDE, 2f)
            .workTicks(480)
            .voltage(Voltage.HV)
            .requireTech(Technologies.HYDROMETALLURGY)
            .build()
            .recipe(DATA_GEN, BLUE_TOPAZ.loc("dust"))
            .input(BLUE_TOPAZ, "dust", 2f)
            .input(HYDROGEN_CHLORIDE, 6f)
            .input(SODIUM_HYDROXIDE, 6f)
            .output(ALUMINIUM_OXIDE, 2f)
            .output(SILICON_DIOXIDE)
            .output(SALT_WATER, 12f)
            .output(HYDROGEN_FLUORIDE, 1f)
            .workTicks(480)
            .voltage(Voltage.HV)
            .requireTech(Technologies.HYDROMETALLURGY)
            .build()
            .recipe(DATA_GEN, AMMONIA.fluidLoc())
            .input(NITROGEN, 0.5f)
            .input(HYDROGEN, 1.5f)
            .input(IRON, "dust_tiny", 1f)
            .output(AMMONIA)
            .workTicks(512)
            .voltage(Voltage.HV)
            .requireTech(Technologies.ADVANCED_CHEMISTRY)
            .build()
            .recipe(DATA_GEN, suffix(SULFURIC_ACID.fluidLoc(), "_from_hydrogen_sulfide"))
            .input(HYDROGEN_SULFIDE)
            .input(OXYGEN, 2f)
            .output(SULFURIC_ACID)
            .workTicks(160)
            .voltage(Voltage.HV)
            .requireMultiblock()
            .requireTech(Technologies.ADVANCED_CHEMISTRY)
            .build()
            .recipe(DATA_GEN, suffix(SULFURIC_ACID.fluidLoc(), "_from_sulfur"))
            .input(SULFUR)
            .input(WATER)
            .input(OXYGEN, 1.5f)
            .output(SULFURIC_ACID)
            .workTicks(240)
            .voltage(Voltage.HV)
            .requireMultiblock()
            .requireTech(Technologies.ADVANCED_CHEMISTRY)
            .build()
            .recipe(DATA_GEN, suffix(SODIUM_CARBONATE.loc("dust"), "_from_salt_water"))
            .input(SALT_WATER, 2f)
            .input(AMMONIA)
            .input(CARBON_DIOXIDE)
            .output(SODIUM_CARBONATE)
            .output(AMMONIUM_CHLORIDE)
            .workTicks(160)
            .voltage(Voltage.HV)
            .requireTech(Technologies.ADVANCED_CHEMISTRY)
            .build()
            .recipe(DATA_GEN, NITRIC_ACID.fluidLoc())
            .input(AMMONIA)
            .input(OXYGEN, 2f)
            .output(NITRIC_ACID)
            .output(WATER)
            .workTicks(256)
            .voltage(Voltage.HV)
            .requireMultiblock()
            .requireTech(Technologies.ADVANCED_CHEMISTRY)
            .build()
            .recipe(DATA_GEN, LITHIUM_CARBONATE.loc("dust"))
            .input(LITHIUM_BRINE, 4f)
            .input(SODIUM_CARBONATE)
            .output(LITHIUM_CARBONATE)
            .output(SALT_WATER, 4f)
            .workTicks(128)
            .voltage(Voltage.HV)
            .requireTech(Technologies.ADVANCED_CHEMISTRY)
            .build()
            .recipe(DATA_GEN, TITANIUM_TETRACHLORIDE.fluidLoc())
            .input(RUTILE)
            .input(CHLORINE, 2f)
            .input(CARBON)
            .output(TITANIUM_TETRACHLORIDE)
            .output(CARBON_DIOXIDE)
            .workTicks(320)
            .voltage(Voltage.HV)
            .requireTech(Technologies.ADVANCED_CHEMISTRY)
            .build();

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

        CHEMICAL_REACTOR
            .recipe(DATA_GEN, ETHANOL.fluidLoc())
            .input(BIOMASS, 3f)
            .input(WATER, 2f)
            .output(ETHANOL, 2f)
            .output(CARBON_DIOXIDE)
            .workTicks(400)
            .voltage(Voltage.MV)
            .requireTech(Technologies.ORGANIC_CHEMISTRY)
            .build()
            .recipe(DATA_GEN, suffix(CARBON_DIOXIDE.fluidLoc(), "_from_methane"))
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
            .output(PE, 1.5f)
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
            .output(PVC, 1.5f)
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
            .build()
            .recipe(DATA_GEN, RUBBER.fluidLoc())
            .input(RAW_RUBBER, 9f)
            .input(SULFUR)
            .output(RUBBER, "molten", 9f)
            .workTicks(160)
            .voltage(Voltage.MV)
            .requireTech(Technologies.ORGANIC_CHEMISTRY)
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

        // HV
        CHEMICAL_REACTOR.recipe(DATA_GEN, CHLOROFORM.fluidLoc())
            .input(METHANE)
            .input(CHLORINE, 3f)
            .output(CHLOROFORM)
            .output(HYDROGEN_CHLORIDE, 3f)
            .voltage(Voltage.HV)
            .workTicks(128)
            .requireTech(Technologies.ADVANCED_CHEMISTRY)
            .build()
            .recipe(DATA_GEN, TETRA_FLUORO_ETHYLENE.fluidLoc())
            .input(CHLOROFORM, 2f)
            .input(HYDROGEN_FLUORIDE, 4f)
            .output(TETRA_FLUORO_ETHYLENE)
            .output(HYDROGEN_CHLORIDE, 6f)
            .voltage(Voltage.HV)
            .workTicks(480)
            .requireTech(Technologies.ADVANCED_CHEMISTRY)
            .build()
            .recipe(DATA_GEN, PTFE.fluidLoc())
            .input(TETRA_FLUORO_ETHYLENE, 0.144f)
            .input(OXYGEN)
            .output(PTFE, 1.5f)
            .voltage(Voltage.HV)
            .workTicks(160)
            .requireTech(Technologies.ADVANCED_CHEMISTRY)
            .build()
            .recipe(DATA_GEN, suffix(VINYL_CHLORIDE.fluidLoc(), "_from_lcr"))
            .input(ETHYLENE)
            .input(HYDROGEN_CHLORIDE)
            .input(OXYGEN, 0.5f)
            .output(VINYL_CHLORIDE)
            .output(WATER)
            .voltage(Voltage.HV)
            .workTicks(160)
            .requireTech(Technologies.ADVANCED_CHEMISTRY)
            .build()
            .recipe(DATA_GEN, suffix(TETRA_FLUORO_ETHYLENE.fluidLoc(), "_from_lcr"))
            .input(METHANE, 2f)
            .input(CHLORINE, 6f)
            .input(HYDROGEN_FLUORIDE, 4f)
            .output(TETRA_FLUORO_ETHYLENE)
            .output(HYDROGEN_CHLORIDE, 12f)
            .voltage(Voltage.HV)
            .workTicks(320)
            .requireTech(Technologies.ADVANCED_CHEMISTRY)
            .build()
            .recipe(DATA_GEN, suffix(PE.fluidLoc(), "_from_lcr"))
            .input(ETHYLENE, 2.16f)
            .input(OXYGEN, 7.5f)
            .input(TITANIUM_TETRACHLORIDE, 0.1f)
            .output(PE, 30f)
            .voltage(Voltage.HV)
            .workTicks(256)
            .requireTech(Technologies.ADVANCED_CHEMISTRY)
            .build()
            .recipe(DATA_GEN, suffix(PVC.fluidLoc(), "_from_lcr"))
            .input(VINYL_CHLORIDE, 2.16f)
            .input(OXYGEN, 7.5f)
            .input(TITANIUM_TETRACHLORIDE, 0.1f)
            .output(PVC, 30f)
            .voltage(Voltage.HV)
            .workTicks(320)
            .requireTech(Technologies.ADVANCED_CHEMISTRY)
            .build()
            .recipe(DATA_GEN, suffix(PTFE.fluidLoc(), "_from_lcr"))
            .input(TETRA_FLUORO_ETHYLENE, 2.16f)
            .input(OXYGEN, 7.5f)
            .input(TITANIUM_TETRACHLORIDE, 0.1f)
            .output(PTFE, 30f)
            .voltage(Voltage.HV)
            .workTicks(512)
            .requireTech(Technologies.ADVANCED_CHEMISTRY)
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
