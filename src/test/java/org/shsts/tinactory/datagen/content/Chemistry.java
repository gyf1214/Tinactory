package org.shsts.tinactory.datagen.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.material.Fluids;
import org.shsts.tinactory.content.electric.Voltage;

import static org.shsts.tinactory.content.AllItems.STEAM;
import static org.shsts.tinactory.content.AllMaterials.AIR;
import static org.shsts.tinactory.content.AllMaterials.ARGON;
import static org.shsts.tinactory.content.AllMaterials.CALCIUM_CARBONATE;
import static org.shsts.tinactory.content.AllMaterials.CALCIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.CARBON;
import static org.shsts.tinactory.content.AllMaterials.CARBON_DIOXIDE;
import static org.shsts.tinactory.content.AllMaterials.CHLORINE;
import static org.shsts.tinactory.content.AllMaterials.HYDROGEN;
import static org.shsts.tinactory.content.AllMaterials.HYDROGEN_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.LITHIUM_BRINE;
import static org.shsts.tinactory.content.AllMaterials.MAGNESIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.NITROGEN;
import static org.shsts.tinactory.content.AllMaterials.OXYGEN;
import static org.shsts.tinactory.content.AllMaterials.POTASSIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.SALT_WATER;
import static org.shsts.tinactory.content.AllMaterials.SEA_WATER;
import static org.shsts.tinactory.content.AllMaterials.SODIUM_CARBONATE;
import static org.shsts.tinactory.content.AllMaterials.SODIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.SODIUM_HYDROXIDE;
import static org.shsts.tinactory.content.AllRecipes.CHEMICAL_REACTOR;
import static org.shsts.tinactory.content.AllRecipes.DISTILLATION;
import static org.shsts.tinactory.content.AllRecipes.STONE_GENERATOR;
import static org.shsts.tinactory.content.AllRecipes.VACUUM_FREEZER;
import static org.shsts.tinactory.test.TinactoryTest.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Chemistry {
    public static void init() {
        inorganic();
    }

    private static void inorganic() {
        STONE_GENERATOR.recipe(DATA_GEN, AIR.fluidLoc())
            .outputFluid(1, AIR.fluid(), AIR.fluidAmount(1))
            .voltage(Voltage.MV)
            .build()
            .recipe(DATA_GEN, SEA_WATER.fluidLoc())
            .outputFluid(1, SEA_WATER.fluid(), SEA_WATER.fluidAmount(1))
            .voltage(Voltage.MV)
            .build();

        VACUUM_FREEZER.recipe(DATA_GEN, AIR.fluidLoc("liquid"))
            .inputFluid(1, AIR.fluid(), AIR.fluidAmount(1))
            .outputFluid(3, AIR.fluid("liquid"), AIR.fluidAmount("liquid", 1))
            .workTicks(200)
            .voltage(Voltage.MV)
            .build();

        DISTILLATION.recipe(DATA_GEN, AIR.fluidLoc("liquid"))
            .inputFluid(0, AIR.fluid("liquid"), AIR.fluidAmount(1))
            .outputFluid(1, NITROGEN.fluid(), NITROGEN.fluidAmount(0.78f))
            .outputFluid(1, OXYGEN.fluid(), OXYGEN.fluidAmount(0.21f))
            .outputFluid(1, ARGON.fluid(), ARGON.fluidAmount(0.01f))
            .workTicks(100)
            .voltage(Voltage.MV)
            .build()
            .recipe(DATA_GEN, SEA_WATER.fluidLoc())
            .inputFluid(0, SEA_WATER.fluid(), SEA_WATER.fluidAmount(10))
            .outputItem(2, SODIUM_CHLORIDE.entry("dust"), 5)
            .outputItem(2, POTASSIUM_CHLORIDE.entry("dust"), 1)
            .outputItem(2, MAGNESIUM_CHLORIDE.entry("dust"), 1, 0.5)
            .outputItem(2, CALCIUM_CHLORIDE.entry("dust"), 1, 0.2)
            .outputFluid(1, STEAM, 6400)
            .outputFluid(1, LITHIUM_BRINE.fluid(), LITHIUM_BRINE.fluidAmount(0.1f))
            .workTicks(4000)
            .voltage(Voltage.MV)
            .build()
            .recipe(DATA_GEN, SALT_WATER.fluidLoc())
            .inputFluid(0, SALT_WATER.fluid(), SALT_WATER.fluidAmount(2))
            .outputItem(2, SODIUM_CHLORIDE.entry("dust"), 1)
            .outputFluid(1, STEAM, 1000)
            .workTicks(800)
            .voltage(Voltage.MV)
            .build()
            .recipe(DATA_GEN, Fluids.WATER)
            .inputFluid(0, Fluids.WATER, 1000)
            .outputFluid(1, STEAM, 1000)
            .workTicks(400)
            .voltage(Voltage.MV)
            .build();

        CHEMICAL_REACTOR.recipe(DATA_GEN, HYDROGEN_CHLORIDE.fluidLoc())
            .inputFluid(1, HYDROGEN.fluid(), HYDROGEN.fluidAmount(1))
            .inputFluid(1, CHLORINE.fluid(), CHLORINE.fluidAmount(1))
            .outputFluid(3, HYDROGEN_CHLORIDE.fluid(), HYDROGEN_CHLORIDE.fluidAmount(1))
            .workTicks(128)
            .voltage(Voltage.MV)
            .build()
            .recipe(DATA_GEN, CARBON_DIOXIDE.fluidLoc())
            .inputItem(0, CARBON.entry("dust"), 1)
            .inputFluid(1, OXYGEN.fluid(), OXYGEN.fluidAmount(1))
            .outputFluid(3, CARBON_DIOXIDE.fluid(), CARBON_DIOXIDE.fluidAmount(1))
            .workTicks(320)
            .voltage(Voltage.MV)
            .build()
            .recipe(DATA_GEN, SODIUM_CARBONATE.loc("dust"))
            .inputItem(0, SODIUM_HYDROXIDE.entry("dust"), 1)
            .inputFluid(1, CARBON_DIOXIDE.fluid(), CARBON_DIOXIDE.fluidAmount(1))
            .outputItem(2, SODIUM_CARBONATE.entry("dust"), 1)
            .workTicks(128)
            .voltage(Voltage.MV)
            .build()
            .recipe(DATA_GEN, CALCIUM_CARBONATE.loc("dust"))
            .inputItem(0, SODIUM_CARBONATE.entry("dust"), 1)
            .inputItem(0, CALCIUM_CHLORIDE.entry("dust"), 1)
            .outputItem(2, CALCIUM_CARBONATE.entry("dust"), 1)
            .outputItem(2, SODIUM_CHLORIDE.entry("dust"), 1)
            .workTicks(128)
            .voltage(Voltage.MV)
            .build();
    }
}
