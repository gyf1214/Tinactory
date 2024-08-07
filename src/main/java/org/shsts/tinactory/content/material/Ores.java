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
import static org.shsts.tinactory.content.AllMaterials.PYRITE;
import static org.shsts.tinactory.content.AllMaterials.REDSTONE;
import static org.shsts.tinactory.content.AllMaterials.RUBY;
import static org.shsts.tinactory.content.AllMaterials.set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Ores {
    static {
        CHALCOPYRITE = set("chalcopyrite")
                .color(0xFFA07828)
                .ore(OreVariant.STONE)
                .buildObject();

        PYRITE = set("pyrite")
                .color(0xFF967828)
                .ore(OreVariant.STONE)
                .buildObject();

        LIMONITE = set("limonite")
                .color(0xFFC8C800)
                .ore(OreVariant.STONE)
                .buildObject();

        BANDED_IRON = set("banded_iron")
                .color(0xFF915A5A)
                .ore(OreVariant.STONE)
                .buildObject();

        COAL = set("coal")
                .color(0xFF464646)
                .existing("primary", Items.COAL)
                .ore(OreVariant.STONE)
                .buildObject();

        CASSITERITE = set("cassiterite")
                .color(0xFFDCDCDC)
                .ore(OreVariant.STONE)
                .buildObject();

        REDSTONE = set("redstone")
                .color(0xFFC80000)
                .existing("dust", Tags.Items.DUSTS_REDSTONE, Items.REDSTONE)
                .ore(OreVariant.STONE)
                .buildObject();

        CINNABAR = set("cinnabar")
                .color(0xFF960000)
                .ore(OreVariant.STONE)
                .buildObject();

        RUBY = set("ruby")
                .color(0xFFFF6464)
                .ore(OreVariant.STONE)
                .buildObject();

//        MAGNETITE = set("magnetite")
//                .color(0xFF1E1E1E)
//                .ore(OreVariant.DEEPSLATE)
//                .buildObject();
    }

    public static void init() {}
}
