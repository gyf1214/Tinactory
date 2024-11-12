package org.shsts.tinactory.content.material;

import net.minecraft.world.item.Tiers;

import static org.shsts.tinactory.content.AllMaterials.BATTERY_ALLOY;
import static org.shsts.tinactory.content.AllMaterials.BRASS;
import static org.shsts.tinactory.content.AllMaterials.BRONZE;
import static org.shsts.tinactory.content.AllMaterials.COBALTITE;
import static org.shsts.tinactory.content.AllMaterials.CUPRONICKEL;
import static org.shsts.tinactory.content.AllMaterials.GALLIUM_ARSENIDE;
import static org.shsts.tinactory.content.AllMaterials.INVAR;
import static org.shsts.tinactory.content.AllMaterials.KANTHAL;
import static org.shsts.tinactory.content.AllMaterials.RED_ALLOY;
import static org.shsts.tinactory.content.AllMaterials.RUTILE;
import static org.shsts.tinactory.content.AllMaterials.SOLDERING_ALLOY;
import static org.shsts.tinactory.content.AllMaterials.STEEL;
import static org.shsts.tinactory.content.AllMaterials.WROUGHT_IRON;
import static org.shsts.tinactory.content.AllMaterials.set;

public final class FirstDegrees {
    static {
        WROUGHT_IRON = set("wrought_iron")
            .color(0xFFC8B4B4)
            .metalExt().nugget()
            .toolSet(200, Tiers.IRON)
            .buildObject();

        BRONZE = set("bronze")
            .color(0xFFFF8000)
            .foil().pipe().rotor().molten()
            .buildObject();

        COBALTITE = set("cobaltite")
            .color(0xFF5050FA)
            .dust()
            .buildObject();

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
            .wireAndPlate()
            .buildObject();

        BATTERY_ALLOY = set("battery_alloy")
            .color(0xFF9C7CA0)
            .plate()
            .buildObject();

        SOLDERING_ALLOY = set("soldering_alloy")
            .color(0xFF9696A0)
            .molten()
            .buildObject();

        RUTILE = set("rutile")
            .color(0xFFD40D5C)
            .dust()
            .buildObject();

        BRASS = set("brass")
            .color(0xFFFFB400)
            .metal().stick().pipe()
            .buildObject();

        GALLIUM_ARSENIDE = set("gallium_arsenide")
            .color(0xFFA0A0A0)
            .dust()
            .buildObject();

        KANTHAL = set("kanthal")
            .color(0xFFC2D2DF)
            .wire()
            .buildObject();
    }

    public static void init() {}
}
