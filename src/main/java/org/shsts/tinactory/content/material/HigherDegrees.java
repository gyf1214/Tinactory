package org.shsts.tinactory.content.material;

import net.minecraft.world.item.Tiers;

import static org.shsts.tinactory.content.AllMaterials.AIR;
import static org.shsts.tinactory.content.AllMaterials.COBALT_BRASS;
import static org.shsts.tinactory.content.AllMaterials.HEAVY_FUEL;
import static org.shsts.tinactory.content.AllMaterials.LIGHT_FUEL;
import static org.shsts.tinactory.content.AllMaterials.NAPHTHA;
import static org.shsts.tinactory.content.AllMaterials.PE;
import static org.shsts.tinactory.content.AllMaterials.PVC;
import static org.shsts.tinactory.content.AllMaterials.REFINERY_GAS;
import static org.shsts.tinactory.content.AllMaterials.SALT_WATER;
import static org.shsts.tinactory.content.AllMaterials.SEA_WATER;
import static org.shsts.tinactory.content.AllMaterials.liquid;
import static org.shsts.tinactory.content.AllMaterials.set;

public final class HigherDegrees {
    static {
        COBALT_BRASS = set("cobalt_brass")
            .color(0xFFB4B4A0)
            .gear()
            .toolSet(1000, Tiers.IRON)
            .buildObject();

        AIR = set("air")
            .color(0xFFA9D0F5)
            .fluid("gas", "air", 1000)
            .fluid("liquid", "liquid_air", 0xFF84BCFC, 1000)
            .fluidPrimary("gas")
            .buildObject();

        SALT_WATER = liquid("salt_water", 0xFF0000C8);
        SEA_WATER = liquid("sea_water", 0xFF0042c8);

        REFINERY_GAS = set("refinery_gas")
            .color(0xFFB4B4B4)
            .fluid("fluid", "refinery_gas", 1000)
            .fluid("sulfuric", "sulfuric_gas", 0xFFECDCCC, 1000)
            .gas("lightly_steam_cracked", 0xFFC8C8C8)
            .gas("severely_steam_cracked", 0xFFE0E0E0)
            .gas("lightly_hydro_cracked", 0xFFA0A0A0)
            .gas("severely_hydro_cracked", 0xFF919191)
            .buildObject();

        NAPHTHA = set("naphtha")
            .color(0xFFFFF404)
            .fluid("fluid", "naphtha", 1000)
            .fluid("sulfuric", "sulfuric_naphtha", 0xFFFCF404, 1000)
            .fluid("lightly_steam_cracked", "lightly_steamcracked_naphtha", 0xFFBFB608, 1000)
            .fluid("severely_steam_cracked", "severely_steamcracked_naphtha", 0xFFCCC434, 1000)
            .fluid("lightly_hydro_cracked", "lightly_hydrocracked_naphtha", 0xFFD4C404, 1000)
            .fluid("severely_hydro_cracked", "severely_hydrocracked_naphtha", 0xFFDCD40C, 1000)
            .buildObject();

        LIGHT_FUEL = set("light_fuel")
            .color(0xFFFCF404)
            .fluid("fluid", "light_fuel", 1000)
            .fluid("sulfuric", "sulfuric_light_fuel", 0xFFFCCC04, 1000)
            .fluid("lightly_steam_cracked", "lightly_steamcracked_light_fuel", 0xFFFCFC0C, 1000)
            .fluid("severely_steam_cracked", "severely_steamcracked_light_fuel", 0xFFFCFC2C, 1000)
            .fluid("lightly_hydro_cracked", "lightly_hydrocracked_light_fuel", 0xFFA49C04, 1000)
            .fluid("severely_hydro_cracked", "severely_hydrocracked_light_fuel", 0xFF847C04, 1000)
            .buildObject();

        HEAVY_FUEL = set("heavy_fuel")
            .color(0xFFFCECAC)
            .fluid("fluid", "heavy_fuel", 1000)
            .fluid("sulfuric", "sulfuric_heavy_fuel", 0xFFFCEC94, 1000)
            .fluid("lightly_hydro_cracked", "lightly_hydrocracked_heavy_fuel", 0xFFD4C494, 1000)
            .fluid("severely_hydro_cracked", "severely_hydrocracked_heavy_fuel", 0xFFBCAC84, 1000)
            .buildObject();

        PE = set("polyethylene")
            .color(0xFFC8C8C8)
            .polymerFoil()
            .buildObject();

        PVC = set("polyvinyl_chloride")
            .color(0xFFD7E6E6)
            .polymerFoil()
            .buildObject();
    }

    public static void init() {}
}
