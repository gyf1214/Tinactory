package org.shsts.tinactory.content.material;

import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.material.Fluids;

import static org.shsts.tinactory.content.AllMaterials.AMMONIA;
import static org.shsts.tinactory.content.AllMaterials.AMMONIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.BATTERY_ALLOY;
import static org.shsts.tinactory.content.AllMaterials.BRASS;
import static org.shsts.tinactory.content.AllMaterials.BRONZE;
import static org.shsts.tinactory.content.AllMaterials.CALCIUM_CARBONATE;
import static org.shsts.tinactory.content.AllMaterials.CALCIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.CALCIUM_HYDROXIDE;
import static org.shsts.tinactory.content.AllMaterials.CARBON_DIOXIDE;
import static org.shsts.tinactory.content.AllMaterials.COBALTITE;
import static org.shsts.tinactory.content.AllMaterials.CUPRONICKEL;
import static org.shsts.tinactory.content.AllMaterials.ELECTRUM;
import static org.shsts.tinactory.content.AllMaterials.GALLIUM_ARSENIDE;
import static org.shsts.tinactory.content.AllMaterials.HYDROGEN_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.HYDROGEN_SULFIDE;
import static org.shsts.tinactory.content.AllMaterials.INVAR;
import static org.shsts.tinactory.content.AllMaterials.KANTHAL;
import static org.shsts.tinactory.content.AllMaterials.LITHIUM_BRINE;
import static org.shsts.tinactory.content.AllMaterials.LITHIUM_CARBONATE;
import static org.shsts.tinactory.content.AllMaterials.LITHIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.MAGNESIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.NITRIC_ACID;
import static org.shsts.tinactory.content.AllMaterials.POTASSIUM_CARBONATE;
import static org.shsts.tinactory.content.AllMaterials.POTASSIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.POTASSIUM_NITRATE;
import static org.shsts.tinactory.content.AllMaterials.RED_ALLOY;
import static org.shsts.tinactory.content.AllMaterials.RUTILE;
import static org.shsts.tinactory.content.AllMaterials.SODIUM_CARBONATE;
import static org.shsts.tinactory.content.AllMaterials.SODIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.SODIUM_HYDROXIDE;
import static org.shsts.tinactory.content.AllMaterials.SOLDERING_ALLOY;
import static org.shsts.tinactory.content.AllMaterials.STAINLESS_STEEL;
import static org.shsts.tinactory.content.AllMaterials.STEEL;
import static org.shsts.tinactory.content.AllMaterials.SULFURIC_ACID;
import static org.shsts.tinactory.content.AllMaterials.WATER;
import static org.shsts.tinactory.content.AllMaterials.WROUGHT_IRON;
import static org.shsts.tinactory.content.AllMaterials.dust;
import static org.shsts.tinactory.content.AllMaterials.fluid;
import static org.shsts.tinactory.content.AllMaterials.gas;
import static org.shsts.tinactory.content.AllMaterials.liquid;
import static org.shsts.tinactory.content.AllMaterials.set;

public final class FirstDegrees {
    static {
        WATER = set("water")
            .color(0xFF0000FF)
            .existing("liquid", Fluids.WATER, 1000)
            .fluidPrimary("liquid")
            .buildObject();

        WROUGHT_IRON = set("wrought_iron")
            .color(0xFFC8B4B4)
            .metalExt().nugget()
            .toolSet(200, Tiers.IRON)
            .buildObject();

        BRONZE = set("bronze")
            .color(0xFFFF8000)
            .foil().pipe().rotor().molten()
            .buildObject();

        COBALTITE = dust("cobaltite", 0xFF5050FA);

        INVAR = set("invar")
            .color(0xFFB4B478)
            .metalExt()
            .buildObject();

        CUPRONICKEL = set("cupronickel")
            .color(0xFFE39680)
            .wireAndPlate()
            .buildObject();

        STEEL = set("steel")
            .color(0xFF808080)
            .mechanical().magnetic().gear().molten()
            .toolSet(800, Tiers.IRON)
            .buildObject();

        RED_ALLOY = set("red_alloy")
            .color(0xFFC80000)
            .plate().wireFine()
            .buildObject();

        BATTERY_ALLOY = set("battery_alloy")
            .color(0xFF9C7CA0)
            .plate()
            .buildObject();

        SOLDERING_ALLOY = set("soldering_alloy")
            .color(0xFF9696A0)
            .molten()
            .buildObject();

        RUTILE = dust("rutile", 0xFFD40D5C);

        BRASS = set("brass")
            .color(0xFFFFB400)
            .metal().stick().pipe()
            .buildObject();

        GALLIUM_ARSENIDE = dust("gallium_arsenide", 0xFFA0A0A0);

        KANTHAL = set("kanthal")
            .color(0xFFC2D2DF)
            .wire()
            .buildObject();

        ELECTRUM = set("electrum")
            .color(0xFFFFFF64)
            .metalExt().wireFine().foil()
            .buildObject();

        STAINLESS_STEEL = set("stainless_steel")
            .color(0xFFC8C8DC)
            .mechanical().gear().molten()
            .toolSet(1200, Tiers.IRON)
            .buildObject();

        SODIUM_CHLORIDE = dust("sodium_chloride", 0xFFFAFAFA);
        POTASSIUM_CHLORIDE = dust("potassium_chloride", 0xFFF0C8C8);
        MAGNESIUM_CHLORIDE = dust("magnesium_chloride", 0xFFD40D5C);
        CALCIUM_CHLORIDE = dust("calcium_chloride", 0xFFEBEBFA);
        LITHIUM_CHLORIDE = dust("lithium_chloride", 0xFFDEDEFA);
        AMMONIUM_CHLORIDE = dust("ammonium_chloride", 0xFF9711A6);
        SODIUM_CARBONATE = dust("sodium_carbonate", 0xFFDCDCFF);
        POTASSIUM_CARBONATE = dust("potassium_carbonate", 0xFF784137);
        CALCIUM_CARBONATE = dust("calcium_carbonate", 0xFFFAE6DC);
        LITHIUM_CARBONATE = dust("lithium_carbonate", 0xFFBEBEDA);
        POTASSIUM_NITRATE = dust("potassium_nitrate", 0xFFE6E6E6);
        SODIUM_HYDROXIDE = dust("sodium_hydroxide", 0xFF003380);
        CALCIUM_HYDROXIDE = dust("calcium_hydroxide", 0xFFF0F0F0);

        LITHIUM_BRINE = liquid("lithium_brine", 0xFF4200C7);
        CARBON_DIOXIDE = gas("carbon_dioxide", 0xFFA9D0F5);
        HYDROGEN_CHLORIDE = fluid("hydrogen_chloride", 0xFFBCBCB5, "gas", "hydrochloric_acid");
        HYDROGEN_SULFIDE = fluid("hydrogen_sulfide", 0xFFFC5304, "gas");
        SULFURIC_ACID = fluid("sulfuric_acid", 0xFFFC5304, "liquid");
        NITRIC_ACID = liquid("nitric_acid", 0xFFCCCC00);
        AMMONIA = gas("ammonia", 0xFF3F3480);
    }

    public static void init() {}
}
