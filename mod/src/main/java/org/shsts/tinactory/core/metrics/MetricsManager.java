package org.shsts.tinactory.core.metrics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.IForgeRegistry;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.metrics.IMetricsCallback;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.core.recipe.StackIngredient;
import org.shsts.tinactory.core.recipe.StackResult;

import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MetricsManager {
    private static final List<IMetricsCallback> callbacks = new ArrayList<>();

    public static void onBake(IForgeRegistry<IMetricsCallback> registry) {
        callbacks.clear();
        callbacks.addAll(registry.getValues());
    }

    public static void report(String name, List<String> labels, double value) {
        for (var cb : callbacks) {
            cb.report(name, labels, value);
        }
    }

    public static String ownerName(IMachine machine) {
        return machine.owner().map(ITeamProfile::getName).orElse("unknown");
    }

    public static void reportItem(String name, IMachine machine, ItemStack item) {
        if (!item.isEmpty()) {
            report(name, List.of(ownerName(machine), item.getDescriptionId()), item.getCount());
        }
    }

    public static void reportFluid(String name, IMachine machine, FluidStack fluid) {
        if (!fluid.isEmpty()) {
            report(name, List.of(ownerName(machine), fluid.getTranslationKey()), fluid.getAmount());
        }
    }

    public static void reportProcessingObject(String action, IMachine machine, IProcessingObject object) {
        if (object instanceof StackIngredient<?> ingredient && ingredient.type() == PortType.ITEM) {
            reportItem("item_" + action, machine, (ItemStack) ingredient.stack());
        } else if (object instanceof StackIngredient<?> ingredient && ingredient.type() == PortType.FLUID) {
            reportFluid("fluid_" + action, machine, (FluidStack) ingredient.stack());
        } else if (object instanceof StackResult<?> result && result.type() == PortType.ITEM) {
            reportItem("item_" + action, machine, (ItemStack) result.stack());
        } else if (object instanceof StackResult<?> result && result.type() == PortType.FLUID) {
            reportFluid("fluid_" + action, machine, (FluidStack) result.stack());
        }
    }
}
