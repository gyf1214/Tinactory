package org.shsts.tinactory.content.material;

import net.minecraft.world.item.Tiers;

import static org.shsts.tinactory.content.AllMaterials.BIOMASS;
import static org.shsts.tinactory.content.AllMaterials.COBALT_BRASS;
import static org.shsts.tinactory.content.AllMaterials.CREOSOTE_OIL;
import static org.shsts.tinactory.content.AllMaterials.DIESEL;
import static org.shsts.tinactory.content.AllMaterials.HEAVY_FUEL;
import static org.shsts.tinactory.content.AllMaterials.LIGHT_FUEL;
import static org.shsts.tinactory.content.AllMaterials.LPG;
import static org.shsts.tinactory.content.AllMaterials.NAPHTHA;
import static org.shsts.tinactory.content.AllMaterials.PE;
import static org.shsts.tinactory.content.AllMaterials.PTFE;
import static org.shsts.tinactory.content.AllMaterials.PVC;
import static org.shsts.tinactory.content.AllMaterials.REFINERY_GAS;
import static org.shsts.tinactory.content.AllMaterials.SALT_WATER;
import static org.shsts.tinactory.content.AllMaterials.SEA_WATER;
import static org.shsts.tinactory.content.AllMaterials.VANADIUM_STEEL;
import static org.shsts.tinactory.content.AllMaterials.liquid;
import static org.shsts.tinactory.content.AllMaterials.set;

public final class HigherDegrees {
    static {
        COBALT_BRASS = set("cobalt_brass")
            .color(0xFFB4B4A0)
            .gear()
            .toolSet(600, Tiers.IRON)
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

        BIOMASS = set("biomass")
            .color(0xFF14CC04)
            .fluid("fluid", "biomass", 1000)
            .buildObject();

        LPG = set("lpg")
            .color(0xFFFCFCAC)
            .fluid("fluid", "lpg", 1000)
            .buildObject();

        DIESEL = set("diesel")
            .color(0xFFFCF404)
            .fluid("fluid", "diesel", 1000)
            .buildObject();

        CREOSOTE_OIL = set("creosote_oil")
            .color(0xFF804000)
            .fluid("fluid", "creosote", 1000)
            .buildObject();

        VANADIUM_STEEL = set("vanadium_steel")
            .color(0xFFC0C0C0)
            .hot().gear()
            .toolSet(1200, Tiers.DIAMOND)
            .buildObject();

        PTFE = set("polytetrafluoro_ethylene")
            .color(0xFF646464)
            .polymer()
            .dummies("pipe")
            .buildObject();
    }

    public static void init() {}
}
