package org.shsts.tinactory.content.material;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.content.AllMaterials.BANDED_IRON;
import static org.shsts.tinactory.content.AllMaterials.CASSITERITE;
import static org.shsts.tinactory.content.AllMaterials.CHALCOPYRITE;
import static org.shsts.tinactory.content.AllMaterials.CINNABAR;
import static org.shsts.tinactory.content.AllMaterials.COAL;
import static org.shsts.tinactory.content.AllMaterials.LIMONITE;
import static org.shsts.tinactory.content.AllMaterials.MAGNETITE;
import static org.shsts.tinactory.content.AllMaterials.PYRITE;
import static org.shsts.tinactory.content.AllMaterials.REDSTONE;
import static org.shsts.tinactory.content.AllMaterials.RUBY;
import static org.shsts.tinactory.content.AllMaterials.dust;
import static org.shsts.tinactory.content.AllMaterials.ingot;
import static org.shsts.tinactory.content.AllMaterials.primary;
import static org.shsts.tinactory.content.AllMaterials.set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Ores {
    static {
        CHALCOPYRITE = set("chalcopyrite")
                .color(0xFFA07828).icon(IconSet.DULL)
                .smelt(ingot("copper"))
                .ore(OreVariant.STONE)
                .primitive()
                // TODO: pyrite, cobalt, cadmium?
                .byproduct("wash", dust("pyrite"))
                .build().buildObject();

        PYRITE = set("pyrite")
                .color(0xFF967828).icon(IconSet.ROUGH)
                .smelt(ingot("iron"))
                .ore(OreVariant.STONE)
                .primitive()
                // TODO: sulfur, tricalcium phosphate?
                .build().buildObject();

        LIMONITE = set("limonite")
                .color(0xFFC8C800).icon(IconSet.METALLIC)
                .smelt(ingot("iron"))
                .ore(OreVariant.STONE)
                // TODO: nickel, cobalt
                .byproduct("wash", dust("nickel"))
                .build().buildObject();

        BANDED_IRON = set("banded_iron")
                .color(0xFF915A5A).icon(IconSet.DULL)
                .smelt(ingot("iron"))
                .ore(OreVariant.STONE)
                // TODO: magnetite, magnesium?
                .byproduct("wash", dust("magnetite"))
                .build().buildObject();

        COAL = set("coal")
                .color(0xFF464646).icon(IconSet.DULL)
                .existing("primary", Items.COAL)
                .ore(OreVariant.STONE, 2)
                // TODO: coal, thorium
                .byproduct(primary("coal"))
                .build().buildObject();

        CASSITERITE = set("cassiterite")
                .color(0xFFDCDCDC).icon(IconSet.METALLIC)
                .smelt(ingot("tin"))
                .ore(OreVariant.STONE, 2)
                // TODO: tin, zinc
                .byproduct(dust("tin"))
                .build().buildObject();

        REDSTONE = set("redstone")
                .color(0xFFC80000).icon(IconSet.DULL)
                .existing("dust", Tags.Items.DUSTS_REDSTONE, Items.REDSTONE)
                .ore(OreVariant.STONE, 5)
                // TODO: cinnabar, glowstone, rare earth?
                .build().buildObject();

        CINNABAR = set("cinnabar")
                .color(0xFF960000).icon(IconSet.SHINY)
                .ore(OreVariant.STONE)
                // TODO: glowstone, rare earth?
                .byproduct(dust("redstone"))
                .build().buildObject();

        RUBY = set("ruby")
                .color(0xFFFF6464).icon(IconSet.RUBY)
                .ore(OreVariant.STONE)
                // TODO: cinnabar, chromium
                .byproduct(dust("cinnabar"))
                .build().buildObject();

        MAGNETITE = set("magnetite")
                .color(0xFF1E1E1E).icon(IconSet.METALLIC)
                .smelt(ingot("iron"))
                .ore(OreVariant.DEEPSLATE)
                .byproduct(dust("gold"))
                .byproduct("wash", dust("iron"))
                .build()
                .buildObject();
    }

    public static void init() {}
}
