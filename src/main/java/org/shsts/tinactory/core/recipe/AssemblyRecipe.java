package org.shsts.tinactory.core.recipe;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.tech.Technology;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AssemblyRecipe extends ProcessingRecipe<AssemblyRecipe> {
    private final List<Technology> requiredTech;

    public AssemblyRecipe(RecipeTypeEntry<AssemblyRecipe, ?> type, ResourceLocation loc,
                          List<Input> inputs, List<Output> outputs, long workTicks,
                          long voltage, long power, List<Technology> requiredTech) {
        super(type, loc, inputs, outputs, workTicks, voltage, power);
        this.requiredTech = requiredTech;
    }

    @Override
    public boolean canCraftIn(IContainer container) {
        var ownerTeam = container.getOwnerTeam();
        if (ownerTeam.isEmpty()) {
            return false;
        }
        var team = ownerTeam.get();
        for (var tech : requiredTech) {
            if (!team.isTechFinished(tech)) {
                return false;
            }
        }
        return true;
    }

    public static class Builder extends ProcessingRecipe.Builder<AssemblyRecipe, Builder> {
        private final List<ResourceLocation> requiredTech = new ArrayList<>();

        public Builder(Registrate registrate, RecipeTypeEntry<AssemblyRecipe, Builder> parent,
                       ResourceLocation loc) {
            super(registrate, parent, loc);
        }

        private List<Technology> getRequiredTech() {
            return requiredTech.stream()
                    .flatMap(loc -> TechManager.techByKey(loc).stream())
                    .toList();
        }

        public Builder requireTech(ResourceLocation loc) {
            requiredTech.add(loc);
            return self();
        }

        @Override
        public AssemblyRecipe createObject() {
            return new AssemblyRecipe(parent, loc, getInputs(), getOutputs(), workTicks, voltage, power,
                    getRequiredTech());
        }
    }
}
