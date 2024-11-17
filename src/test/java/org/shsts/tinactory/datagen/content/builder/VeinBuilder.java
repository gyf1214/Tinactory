package org.shsts.tinactory.datagen.content.builder;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.content.material.OreVariant;
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe;
import org.shsts.tinactory.datagen.DataGen;
import org.shsts.tinactory.datagen.builder.DataBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.content.AllRecipes.ORE_ANALYZER;
import static org.shsts.tinactory.content.AllRecipes.RESEARCH_BENCH;
import static org.shsts.tinactory.datagen.content.Technologies.BASE_ORE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VeinBuilder<P> extends DataBuilder<P, VeinBuilder<P>> {
    private final String id;
    private final double rate;
    private final OreAnalyzerRecipe.Builder builder;
    private final List<MaterialSet> ores = new ArrayList<>();
    private boolean baseOre = false;
    private boolean primitive = false;
    private OreVariant variant = null;

    public VeinBuilder(DataGen dataGen, P parent, String id, double rate) {
        super(dataGen, parent, id);
        this.id = id;
        this.rate = rate;
        this.builder = ORE_ANALYZER.recipe(dataGen, id).rate(rate);
    }

    public VeinBuilder<P> ore(MaterialSet material, double rate) {
        builder.outputItem(ores.size() + 1, material.entry("raw"), 1, rate);
        ores.add(material);
        if (variant == null) {
            variant = material.oreVariant();
            builder.inputOre(variant);
        }
        assert variant == material.oreVariant();
        return this;
    }

    public VeinBuilder<P> primitive() {
        builder.primitive();
        primitive = true;
        baseOre = true;
        return this;
    }

    public VeinBuilder<P> base() {
        baseOre = true;
        return this;
    }

    @Override
    protected void register() {
        assert variant != null;
        assert rate > 0d;
        assert !ores.isEmpty();
        if (!primitive) {
            builder.voltage(variant.voltage);
        }
        var tech = BASE_ORE.get(variant);
        var baseProgress = 30L;
        if (!baseOre) {
            tech = dataGen.tech("ore/" + id)
                .maxProgress(baseProgress)
                .displayItem(ores.get(0).loc("raw"))
                .depends(tech).buildLoc();

            RESEARCH_BENCH.recipe(dataGen, tech)
                .target(tech)
                .defaultInput(variant.voltage)
                .build();
        }

        if (!primitive) {
            builder.requireTech(tech);
        }
        builder.build();
    }
}
