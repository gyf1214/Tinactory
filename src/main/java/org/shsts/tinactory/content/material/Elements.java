package org.shsts.tinactory.content.material;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;

import static org.shsts.tinactory.content.AllMaterials.ALUMINIUM;
import static org.shsts.tinactory.content.AllMaterials.ANTIMONY;
import static org.shsts.tinactory.content.AllMaterials.ARGON;
import static org.shsts.tinactory.content.AllMaterials.ARSENIC;
import static org.shsts.tinactory.content.AllMaterials.BERYLLIUM;
import static org.shsts.tinactory.content.AllMaterials.CADMIUM;
import static org.shsts.tinactory.content.AllMaterials.CALCIUM;
import static org.shsts.tinactory.content.AllMaterials.CARBON;
import static org.shsts.tinactory.content.AllMaterials.CHLORINE;
import static org.shsts.tinactory.content.AllMaterials.CHROME;
import static org.shsts.tinactory.content.AllMaterials.COBALT;
import static org.shsts.tinactory.content.AllMaterials.COPPER;
import static org.shsts.tinactory.content.AllMaterials.GALLIUM;
import static org.shsts.tinactory.content.AllMaterials.GOLD;
import static org.shsts.tinactory.content.AllMaterials.HYDROGEN;
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllMaterials.LEAD;
import static org.shsts.tinactory.content.AllMaterials.LITHIUM;
import static org.shsts.tinactory.content.AllMaterials.MAGNESIUM;
import static org.shsts.tinactory.content.AllMaterials.MANGANESE;
import static org.shsts.tinactory.content.AllMaterials.NEODYMIUM;
import static org.shsts.tinactory.content.AllMaterials.NICKEL;
import static org.shsts.tinactory.content.AllMaterials.NITROGEN;
import static org.shsts.tinactory.content.AllMaterials.OXYGEN;
import static org.shsts.tinactory.content.AllMaterials.POTASSIUM;
import static org.shsts.tinactory.content.AllMaterials.SILICON;
import static org.shsts.tinactory.content.AllMaterials.SILVER;
import static org.shsts.tinactory.content.AllMaterials.SODIUM;
import static org.shsts.tinactory.content.AllMaterials.SULFUR;
import static org.shsts.tinactory.content.AllMaterials.THORIUM;
import static org.shsts.tinactory.content.AllMaterials.TIN;
import static org.shsts.tinactory.content.AllMaterials.TITANIUM;
import static org.shsts.tinactory.content.AllMaterials.VANADIUM;
import static org.shsts.tinactory.content.AllMaterials.ZINC;
import static org.shsts.tinactory.content.AllMaterials.dust;
import static org.shsts.tinactory.content.AllMaterials.fluid;
import static org.shsts.tinactory.content.AllMaterials.gas;
import static org.shsts.tinactory.content.AllMaterials.set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Elements {
    static {
        IRON = set("iron")
            .color(0xFFC8C8C8)
            .existing("ingot", Items.IRON_INGOT)
            .existing("nugget", Items.IRON_NUGGET)
            .nugget().wire().rotor().molten()
            .buildObject();

        GOLD = set("gold")
            .color(0xFFFFE650)
            .existing("raw", Items.RAW_GOLD)
            .existing("ingot", Items.GOLD_INGOT)
            .existing("nugget", Items.GOLD_NUGGET)
            .bolt().wireFine().molten()
            .ore(OreVariant.DEEPSLATE)
            .buildObject();

        COPPER = set("copper")
            .color(0xFFFF6400)
            .existing("ingot", Items.COPPER_INGOT)
            .foil().wireFine().bolt().pipe().molten()
            .buildObject();

        TIN = set("tin")
            .color(0xFFDCDCDC)
            .wireFine().bolt().rotor().molten()
            .ore(OreVariant.STONE)
            .buildObject();

        SULFUR = dust("sulfur", 0xFFC8C800);

        COBALT = set("cobalt")
            .color(0xFF5050FA)
            .metalExt()
            .toolSet(400, Tiers.IRON)
            .buildObject();

        CADMIUM = dust("cadmium", 0xFF32323C);

        NICKEL = set("nickel")
            .color(0xFFC8C8FA)
            .metal().molten()
            .buildObject();

        MAGNESIUM = set("magnesium")
            .color(0xFFFFC8C8)
            .dustPrimary().molten()
            .buildObject();

        THORIUM = dust("thorium", 0xFF001E00);

        CHROME = set("chrome")
            .color(0xFFEAC4D8)
            .hot().stick().molten()
            .buildObject();

        ANTIMONY = set("antimony")
            .color(0xFFDCDCF0)
            .metal()
            .buildObject();

        SILVER = set("silver")
            .color(0xFFDCDCFF)
            .foil().wireFine().bolt().molten()
            .ore(OreVariant.DEEPSLATE)
            .buildObject();

        VANADIUM = set("vanadium")
            .color(0xFF323232)
            .dustPrimary().molten()
            .buildObject();

        ALUMINIUM = set("aluminium")
            .color(0xFF80C8F0)
            .mechanical().foil().gear().wire().molten()
            .buildObject();

        LEAD = set("lead")
            .color(0xFF8C648C)
            .metal().molten()
            .buildObject();

        ZINC = set("zinc")
            .color(0xFFEBEBFA)
            .metal().foil().molten()
            .buildObject();

        GALLIUM = set("gallium")
            .color(0xFFDCDCFF)
            .metal().foil().molten()
            .buildObject();

        CARBON = set("carbon")
            .color(0xFF141414)
            .dustPrimary().molten()
            .buildObject();

        MANGANESE = dust("manganese", 0xFFCDE1B9);
        ARSENIC = dust("arsenic", 0xFF676756);

        SILICON = set("silicon")
            .color(0xFF3C3C50)
            .dustPrimary().molten()
            .buildObject();

        OXYGEN = gas("oxygen", 0xFF6688DD);
        NITROGEN = gas("nitrogen", 0xFF00BFC1);
        ARGON = gas("argon", 0xFF00FF00);
        HYDROGEN = gas("hydrogen", 0xFF0000B5);
        CHLORINE = fluid("chlorine", 0xFF2D8C8C, "gas");

        BERYLLIUM = set("beryllium")
            .color(0xFF64B464)
            .plate().molten()
            .buildObject();

        SODIUM = dust("sodium", 0xFF000096);
        POTASSIUM = dust("potassium", 0xFFBEDCFF);
        CALCIUM = dust("calcium", 0xFFFFF5DE);
        LITHIUM = dust("lithium", 0xFFBDC7DB);

        TITANIUM = set("titanium")
            .color(0xFFDCA0F0)
            .mechanical().hot().gear().molten().pipe()
            .buildObject();

        NEODYMIUM = set("neodymium")
            .color(0xFF646464)
            .magnetic()
            .buildObject();
    }

    public static void init() {}
}
