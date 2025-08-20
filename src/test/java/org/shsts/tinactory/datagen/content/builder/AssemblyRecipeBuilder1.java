package org.shsts.tinactory.datagen.content.builder;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.level.ItemLike;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.core.builder.SimpleBuilder;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinycorelib.api.core.Transformer;

import java.util.Map;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AssemblyRecipeBuilder1<P> extends SimpleBuilder<Unit, P, AssemblyRecipeBuilder1<P>> {
    private final Voltage voltage;
    @Nullable
    private final AssemblyRecipe.Builder builder;

    public AssemblyRecipeBuilder1(P parent) {
        super(parent);
        this.voltage = Voltage.PRIMITIVE;
        this.builder = null;
    }

    public AssemblyRecipeBuilder1(P parent, Voltage voltage, AssemblyRecipe.Builder builder) {
        super(parent);
        this.voltage = voltage;
        this.builder = builder;
    }

    public Voltage voltage() {
        return voltage;
    }

    public AssemblyRecipeBuilder1<P> circuit(Voltage v, int count) {
        if (builder != null) {
            builder.inputItem(AllTags.circuit(v), count);
        }
        return this;
    }

    public AssemblyRecipeBuilder1<P> circuit(int count) {
        return circuit(voltage, count);
    }

    public AssemblyRecipeBuilder1<P> component(
        Map<Voltage, ? extends Supplier<? extends ItemLike>> component, int count) {
        if (builder != null) {
            builder.inputItem(component.get(voltage), count);
        }
        return this;
    }

    public AssemblyRecipeBuilder1<P> material(MaterialSet material, String sub, int count) {
        if (builder != null) {
            builder.inputItem(material.tag(sub), count);
        }
        return this;
    }

    public AssemblyRecipeBuilder1<P> materialFluid(MaterialSet material, float count) {
        if (builder != null) {
            builder.inputFluid(material.fluid(), material.fluidAmount(count));
        }
        return this;
    }

    public AssemblyRecipeBuilder1<P> tech(ResourceLocation... loc) {
        if (builder != null) {
            builder.requireTech(loc);
        }
        return this;
    }

    public AssemblyRecipeBuilder1<P> item(Supplier<? extends ItemLike> item, int count) {
        if (builder != null) {
            builder.inputItem(item, count);
        }
        return this;
    }

    public AssemblyRecipeBuilder1<P> transformBuilder(Transformer<AssemblyRecipe.Builder> trans) {
        if (builder != null) {
            builder.transform(trans);
        }
        return this;
    }

    @Override
    protected Unit createObject() {
        if (builder != null) {
            builder.build();
        }
        return Unit.INSTANCE;
    }
}
