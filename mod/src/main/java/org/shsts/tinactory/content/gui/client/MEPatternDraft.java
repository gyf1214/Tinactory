package org.shsts.tinactory.content.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.PortConstraint;
import org.shsts.tinactory.core.autocraft.pattern.RecipeTypeConstraint;
import org.shsts.tinactory.core.autocraft.pattern.TargetRecipeConstraint;
import org.shsts.tinactory.core.autocraft.pattern.VoltageConstraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MEPatternDraft {
    private static final UUID DRAFT_UUID = new UUID(0L, 0L);

    private final List<MEPatternIngredientDraft> inputRows = new ArrayList<>();
    private final List<MEPatternIngredientDraft> outputRows = new ArrayList<>();
    @Nullable
    private ResourceLocation recipeTypeId;
    @Nullable
    private ResourceLocation targetRecipeId;
    @Nullable
    private Integer voltageTier;

    private MEPatternDraft() {}

    public static MEPatternDraft empty() {
        return new MEPatternDraft();
    }

    public static MEPatternDraft fromPattern(CraftPattern pattern) {
        var ret = empty();
        ret.inputRows.addAll(fromAmounts(pattern.inputs(), pattern.constraints(), PortDirection.INPUT));
        ret.outputRows.addAll(fromAmounts(pattern.outputs(), pattern.constraints(), PortDirection.OUTPUT));
        ret.recipeTypeId = recipeType(pattern.constraints()).orElse(null);
        ret.targetRecipeId = targetRecipe(pattern.constraints()).orElse(null);
        ret.voltageTier = voltageConstraintTier(pattern.constraints()).orElse(null);
        return ret;
    }

    /**
     * This function performs a shallow copy. The parameter should not be used.
     */
    public void copyFrom(MEPatternDraft draft) {
        inputRows.clear();
        outputRows.clear();
        inputRows.addAll(draft.inputRows);
        outputRows.addAll(draft.outputRows);
        recipeTypeId = draft.recipeTypeId;
        targetRecipeId = draft.targetRecipeId;
        voltageTier = draft.voltageTier;
    }

    public List<MEPatternIngredientDraft> inputRows() {
        return inputRows;
    }

    public List<MEPatternIngredientDraft> outputRows() {
        return outputRows;
    }

    @Nullable
    public ResourceLocation recipeTypeId() {
        return recipeTypeId;
    }

    public void setRecipeTypeId(@Nullable ResourceLocation value) {
        recipeTypeId = value;
    }

    @Nullable
    public ResourceLocation targetRecipeId() {
        return targetRecipeId;
    }

    public void setTargetRecipeId(@Nullable ResourceLocation value) {
        targetRecipeId = value;
    }

    @Nullable
    public Integer voltageTier() {
        return voltageTier;
    }

    public void setVoltageTier(@Nullable Integer value) {
        voltageTier = value == null || value < 0 ? null : value;
    }

    public Optional<CraftPattern> toPattern() {
        var outputs = toAmounts(outputRows);
        if (outputs.isEmpty()) {
            return Optional.empty();
        }
        var constraints = constraints();
        if (recipeTypeId != null) {
            constraints.add(new RecipeTypeConstraint(recipeTypeId));
        }
        if (targetRecipeId != null) {
            constraints.add(new TargetRecipeConstraint(targetRecipeId));
        }
        if (voltageTier != null) {
            constraints.add(new VoltageConstraint(voltageTier));
        }
        return Optional.of(new CraftPattern(DRAFT_UUID, toAmounts(inputRows), outputs, constraints));
    }

    private List<IMachineConstraint> constraints() {
        var ret = new ArrayList<IMachineConstraint>();
        addPortConstraints(ret, inputRows, PortDirection.INPUT);
        addPortConstraints(ret, outputRows, PortDirection.OUTPUT);
        return ret;
    }

    private static void addPortConstraints(List<IMachineConstraint> ret, List<MEPatternIngredientDraft> rows,
        PortDirection direction) {
        var amountIndex = 0;
        for (var row : rows) {
            if (row.isEmpty()) {
                continue;
            }
            if (row.port() != null) {
                ret.add(row.toConstraint(direction, amountIndex));
            }
            amountIndex++;
        }
    }

    private static List<CraftAmount> toAmounts(List<MEPatternIngredientDraft> rows) {
        return rows.stream()
            .filter(row -> !row.isEmpty())
            .map(MEPatternIngredientDraft::toAmount)
            .toList();
    }

    private static List<MEPatternIngredientDraft> fromAmounts(List<CraftAmount> amounts,
        List<IMachineConstraint> constraints, PortDirection direction) {
        var ret = new ArrayList<MEPatternIngredientDraft>();
        for (var amount : amounts) {
            ret.add(MEPatternIngredientDraft.from(amount));
        }
        for (var constraint : constraints) {
            if (constraint instanceof PortConstraint port && port.direction() == direction &&
                port.index() < ret.size()) {
                ret.get(port.index()).setPort(port.port());
            }
        }
        return ret;
    }

    private static Optional<ResourceLocation> recipeType(List<IMachineConstraint> constraints) {
        return constraints.stream()
            .filter(RecipeTypeConstraint.class::isInstance)
            .map(RecipeTypeConstraint.class::cast)
            .map(RecipeTypeConstraint::recipeTypeId)
            .findFirst();
    }

    private static Optional<ResourceLocation> targetRecipe(List<IMachineConstraint> constraints) {
        return constraints.stream()
            .filter(TargetRecipeConstraint.class::isInstance)
            .map(TargetRecipeConstraint.class::cast)
            .map(TargetRecipeConstraint::recipeId)
            .findFirst();
    }

    private static Optional<Integer> voltageConstraintTier(List<IMachineConstraint> constraints) {
        return constraints.stream()
            .filter(VoltageConstraint.class::isInstance)
            .map(VoltageConstraint.class::cast)
            .map(VoltageConstraint::tier)
            .findFirst();
    }
}
