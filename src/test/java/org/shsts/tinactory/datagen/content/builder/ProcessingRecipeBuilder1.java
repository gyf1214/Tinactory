package org.shsts.tinactory.datagen.content.builder;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.core.builder.SimpleBuilder;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ProcessingResults;
import org.shsts.tinycorelib.api.core.Transformer;

import java.util.function.Supplier;

import static org.shsts.tinactory.content.AllMaterials.getMaterial;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingRecipeBuilder1<B extends ProcessingRecipe.BuilderBase<?, B>, P,
    S extends ProcessingRecipeBuilder1<B, P, S>>
    extends SimpleBuilder<Unit, P, S> {
    private final B builder;
    private long voltage = -1;
    private double amperage = -1d;
    private int defaultInputItem = -1;
    private int defaultInputFluid = -1;
    private int defaultOutputItem = -1;
    private int defaultOutputFluid = -1;

    public ProcessingRecipeBuilder1(P parent, B builder) {
        super(parent);
        this.builder = builder;
    }

    public S defaults(int inputItem, int inputFluid,
        int outputItem, int outputFluid) {
        defaultInputItem = inputItem;
        defaultInputFluid = inputFluid;
        defaultOutputItem = outputItem;
        defaultOutputFluid = outputFluid;
        return self();
    }

    public S inputTag(int port, TagKey<Item> tag, int amount) {
        assert port >= 0;
        builder.input(port, () -> new ProcessingIngredients.TagIngredient(tag, amount));
        return self();
    }

    public S inputTag(TagKey<Item> tag, int amount) {
        return inputTag(defaultInputItem, tag, amount);
    }

    public S inputFluid(int port, Supplier<? extends Fluid> fluid, int amount) {
        assert port >= 0;
        builder.input(port, () -> new ProcessingIngredients.FluidIngredient(
            new FluidStack(fluid.get(), amount)));
        return self();
    }

    public S inputFluid(Supplier<? extends Fluid> fluid, int amount) {
        return inputFluid(defaultInputFluid, fluid, amount);
    }

    public S inputMaterial(int port, String name, String sub, Number amount) {
        var mat = getMaterial(name);
        if (mat.hasFluid(sub)) {
            return inputFluid(port, mat.fluid(sub), mat.fluidAmount(amount.floatValue()));
        } else {
            return inputTag(port, mat.tag(sub), amount.intValue());
        }
    }

    public S inputMaterial(String name, String sub, Number amount) {
        var mat = getMaterial(name);
        if (mat.hasFluid(sub)) {
            return inputFluid(mat.fluid(sub), mat.fluidAmount(amount.floatValue()));
        } else {
            return inputTag(mat.tag(sub), amount.intValue());
        }
    }

    public S outputItem(int port, Supplier<? extends ItemLike> item, int amount, double rate) {
        assert port >= 0;
        builder.output(port, () ->
            new ProcessingResults.ItemResult(rate, new ItemStack(item.get(), amount)));
        return self();
    }

    public S outputItem(int port, Supplier<? extends ItemLike> item, int amount) {
        return outputItem(port, item, amount, 1d);
    }

    public S outputItem(Supplier<? extends ItemLike> item, int amount, double rate) {
        return outputItem(defaultOutputItem, item, amount, rate);
    }

    public S outputItem(Supplier<? extends ItemLike> item, int amount) {
        return outputItem(item, amount, 1d);
    }

    public S outputFluid(int port, Supplier<? extends Fluid> fluid, int amount, double rate) {
        assert port >= 0;
        builder.output(port, () -> new ProcessingResults.FluidResult(
            rate, new FluidStack(fluid.get(), amount)));
        return self();
    }

    public S outputFluid(int port, Supplier<? extends Fluid> fluid, int amount) {
        return outputFluid(port, fluid, amount, 1d);
    }

    public S outputFluid(Supplier<? extends Fluid> fluid, int amount, double rate) {
        return outputFluid(defaultOutputFluid, fluid, amount, rate);
    }

    public S outputFluid(Supplier<? extends Fluid> fluid, int amount) {
        return outputFluid(fluid, amount, 1d);
    }

    public S outputMaterial(int port, String name, String sub, Number amount, double rate) {
        var mat = getMaterial(name);
        if (mat.hasFluid(sub)) {
            return outputFluid(port, mat.fluid(sub),
                mat.fluidAmount(amount.floatValue()), rate);
        } else {
            return outputItem(port, mat.entry(sub), amount.intValue(), rate);
        }
    }

    public S outputMaterial(int port, String name, String sub, Number amount) {
        return outputMaterial(port, name, sub, amount, 1d);
    }

    public S outputMaterial(String name, String sub, Number amount, double rate) {
        var mat = getMaterial(name);
        if (mat.hasFluid(sub)) {
            return outputFluid(mat.fluid(sub), mat.fluidAmount(amount.floatValue()), rate);
        } else {
            return outputItem(mat.entry(sub), amount.intValue(), rate);
        }
    }

    public S outputMaterial(String name, String sub, Number amount) {
        return outputMaterial(name, sub, amount, 1d);
    }

    public S extra(Transformer<B> trans) {
        builder.transform(trans);
        return self();
    }

    public S workTicks(long val) {
        builder.workTicks(val);
        return self();
    }

    public S amperage(double val) {
        amperage = val;
        return self();
    }

    public S voltage(Voltage val) {
        voltage = val.value;
        return self();
    }

    @Override
    protected Unit createObject() {
        assert amperage >= 0 && voltage >= 0;
        var voltage1 = voltage == 0 ? Voltage.ULV.value : voltage;
        builder.voltage(voltage).power((long) (voltage1 * amperage)).build();
        return Unit.INSTANCE;
    }
}
