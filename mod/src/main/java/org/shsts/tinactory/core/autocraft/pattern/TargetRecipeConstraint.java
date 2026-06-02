package org.shsts.tinactory.core.autocraft.pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineProcessor;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record TargetRecipeConstraint(ResourceLocation recipeId) implements IMachineConstraint {
    public static final String TYPE_ID = "tinactory:target_recipe";
    public static final Codec<TargetRecipeConstraint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ResourceLocation.CODEC.fieldOf("recipeId").forGetter(TargetRecipeConstraint::recipeId)
    ).apply(instance, TargetRecipeConstraint::new));

    public TargetRecipeConstraint {
        if (recipeId.getPath().isBlank()) {
            throw new IllegalArgumentException("recipeId path must not be blank");
        }
    }

    @Override
    public String typeId() {
        return TYPE_ID;
    }

    @Override
    public boolean matches(IMachine machine, Voltage voltage) {
        return machine.processor()
            .filter(IMachineProcessor.class::isInstance)
            .map(IMachineProcessor.class::cast)
            .filter(processor -> processor.allowTargetRecipe(recipeId))
            .isPresent();
    }

    @Override
    public Optional<Runnable> configureLease(IMachine machine) {
        var previous = machine.config().getString("targetRecipe");
        machine.setConfig(SetMachineConfigPacket.builder().set("targetRecipe", recipeId).get());
        return Optional.of(() -> previous.ifPresentOrElse(
            value -> machine.setConfig(SetMachineConfigPacket.builder().set("targetRecipe", value).get()),
            () -> machine.setConfig(SetMachineConfigPacket.builder().reset("targetRecipe").get())));
    }
}
