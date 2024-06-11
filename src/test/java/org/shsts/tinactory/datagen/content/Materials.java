package org.shsts.tinactory.datagen.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.material.IconSet;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.datagen.builder.MaterialBuilder;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.content.AllMaterials.ALUMINIUM;
import static org.shsts.tinactory.content.AllMaterials.BANDED_IRON;
import static org.shsts.tinactory.content.AllMaterials.BRONZE;
import static org.shsts.tinactory.content.AllMaterials.CASSITERITE;
import static org.shsts.tinactory.content.AllMaterials.CHALCOPYRITE;
import static org.shsts.tinactory.content.AllMaterials.CINNABAR;
import static org.shsts.tinactory.content.AllMaterials.COAL;
import static org.shsts.tinactory.content.AllMaterials.COPPER;
import static org.shsts.tinactory.content.AllMaterials.CUPRONICKEL;
import static org.shsts.tinactory.content.AllMaterials.FLINT;
import static org.shsts.tinactory.content.AllMaterials.GOLD;
import static org.shsts.tinactory.content.AllMaterials.INVAR;
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllMaterials.LIMONITE;
import static org.shsts.tinactory.content.AllMaterials.MAGNETITE;
import static org.shsts.tinactory.content.AllMaterials.NICKEL;
import static org.shsts.tinactory.content.AllMaterials.PYRITE;
import static org.shsts.tinactory.content.AllMaterials.REDSTONE;
import static org.shsts.tinactory.content.AllMaterials.RUBY;
import static org.shsts.tinactory.content.AllMaterials.STEEL;
import static org.shsts.tinactory.content.AllMaterials.STONE;
import static org.shsts.tinactory.content.AllMaterials.TEST;
import static org.shsts.tinactory.content.AllMaterials.TIN;
import static org.shsts.tinactory.content.AllMaterials.WROUGHT_IRON;
import static org.shsts.tinactory.content.material.IconSet.DULL;
import static org.shsts.tinactory.content.material.IconSet.METALLIC;
import static org.shsts.tinactory.content.material.IconSet.ROUGH;
import static org.shsts.tinactory.content.material.IconSet.SHINY;
import static org.shsts.tinactory.datagen.DataGen.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Materials {
    public static void init() {
        FACTORY.material(IRON, METALLIC).build()
                .material(GOLD, SHINY).build()
                .material(COPPER, SHINY).build()
                .material(TIN, DULL).build()
                .material(NICKEL, METALLIC).build()
                .material(ALUMINIUM, DULL).build()
                .material(WROUGHT_IRON, METALLIC).build()
                .material(BRONZE, METALLIC).build()
                .material(INVAR, METALLIC).build()
                .material(CUPRONICKEL, METALLIC).build()
                .material(STEEL, METALLIC).build()
                .material(CHALCOPYRITE, DULL).build()
                .material(PYRITE, ROUGH).build()
                .material(LIMONITE, METALLIC).build()
                .material(BANDED_IRON, DULL).build()
                .material(COAL, DULL).build()
                .material(CASSITERITE, METALLIC).build()
                .material(REDSTONE, DULL).build()
                .material(CINNABAR, SHINY).build()
                .material(RUBY, IconSet.RUBY).build()
                .material(MAGNETITE, METALLIC).build()
                .material(TEST, DULL).build()
                .material(STONE, ROUGH).build()
                .material(FLINT, DULL).build();
    }

    private static class MaterialFactory {
        public MaterialBuilder<MaterialFactory>
        material(MaterialSet material, IconSet icon) {
            return (new MaterialBuilder<>(DATA_GEN, this, material)).icon(icon);
        }
    }

    private static final MaterialFactory FACTORY = new MaterialFactory();
}
