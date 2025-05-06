package org.shsts.tinactory.datagen.content.builder;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Unit;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.content.material.OreVariant;
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe;
import org.shsts.tinactory.core.builder.Builder;
import org.shsts.tinactory.datagen.builder.TechBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.content.AllRecipes.ORE_ANALYZER;
import static org.shsts.tinactory.datagen.builder.TechBuilder.RANK_PER_VOLTAGE;
import static org.shsts.tinactory.datagen.content.Technologies.BASE_ORE;
import static org.shsts.tinactory.datagen.content.Technologies.TECHS;
import static org.shsts.tinactory.test.TinactoryTest.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VeinBuilder<P> extends Builder<Unit, P, VeinBuilder<P>> {
    public static final int VEIN_TECH_RANK = RANK_PER_VOLTAGE / 2;

    private final String id;
    private final int rank;
    private final double rate;
    private final OreAnalyzerRecipe.Builder builder;
    private final List<MaterialSet> ores = new ArrayList<>();
    private boolean baseOre = false;
    private boolean primitive = false;
    private OreVariant variant = null;

    private VeinBuilder(P parent, String id, int rank, double rate) {
        super(parent);
        this.id = id;
        this.rank = rank;
        this.rate = rate;
        this.builder = ORE_ANALYZER.recipe(DATA_GEN, id).rate(rate);
    }

    public static <P> VeinBuilder<P> factory(P parent, String id, int rank, double rate) {
        var builder = new VeinBuilder<>(parent, id, rank, rate);
        return builder.onBuild(builder::onRegister);
    }

    public VeinBuilder<P> ore(MaterialSet material, double rate) {
        builder.outputItem(material.entry("raw"), 1, rate);
        ores.add(material);
        if (variant == null) {
            variant = material.oreVariant();
            builder.inputItem(() -> variant.baseItem, 1);
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
    protected Unit createObject() {
        return Unit.INSTANCE;
    }

    private void onRegister() {
        assert variant != null;
        assert rate > 0d;
        assert !ores.isEmpty();
        if (!primitive) {
            builder.voltage(variant.voltage);
        }
        var tech = BASE_ORE.get(variant);
        var baseProgress = 30L;
        if (!baseOre) {
            tech = TECHS.builder("ore/" + id, TechBuilder::factory)
                .maxProgress(baseProgress)
                .displayItem(ores.get(0).loc("raw"))
                .depends(tech)
                .rank(rank + VEIN_TECH_RANK + 1)
                .researchVoltage(variant.voltage)
                .register();
        }

        if (!primitive) {
            builder.requireTech(tech);
        }
        builder.build();
    }
}
