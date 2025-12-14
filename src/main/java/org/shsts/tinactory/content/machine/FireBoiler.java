package org.shsts.tinactory.content.machine;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import org.shsts.tinactory.api.logistics.ContainerAccess;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.metrics.MetricsManager;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.core.machine.ProcessingMachine.PROGRESS_PER_TICK;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class FireBoiler extends Boiler implements IBoiler {
    private final double burnSpeed;
    private final double burnHeat;

    @Nullable
    private IItemCollection fuelPort;
    private long maxBurn = 0L;
    private ItemStack burningItem = ItemStack.EMPTY;
    private long currentBurn = 0L;
    private boolean needUpdate = true;
    private boolean stopped = false;

    public record Properties(double baseHeat, double baseDecay, double burnSpeed, double burnHeat) {
        public static Properties fromJson(JsonObject jo) {
            return new Properties(
                GsonHelper.getAsDouble(jo, "baseHeat"),
                GsonHelper.getAsDouble(jo, "baseDecay"),
                GsonHelper.getAsDouble(jo, "burnSpeed"),
                GsonHelper.getAsDouble(jo, "burnHeat"));
        }
    }

    public FireBoiler(Properties properties) {
        super(properties.baseHeat(), properties.baseDecay());
        this.burnSpeed = properties.burnSpeed();
        this.burnHeat = properties.burnHeat();
    }

    public void setContainer(IContainer container) {
        fuelPort = container.getPort(0, ContainerAccess.INTERNAL).asItem();
        fuelPort.asItemFilter().setFilters(List.of(item ->
            ForgeHooks.getBurnTime(item, null) > 0 && !item.hasContainerItem()));

        var inputPort = container.getPort(1, ContainerAccess.INTERNAL).asFluid();
        var outputPort = container.getPort(2, ContainerAccess.INTERNAL).asFluid();
        setContainer(inputPort, outputPort);
    }

    @Override
    public void resetContainer() {
        super.resetContainer();
        fuelPort = null;
    }

    public boolean hasContainer() {
        return fuelPort != null;
    }

    protected abstract Optional<IMachine> machine();

    protected abstract double boilParallel();

    protected abstract int burnParallel();

    protected abstract void setChanged();

    public void onUpdateContainer() {
        if (maxBurn == 0) {
            needUpdate = true;
        }
    }

    public void setStopped(boolean val) {
        stopped = val;
    }

    @Override
    public void onPreWork() {
        if (fuelPort == null) {
            return;
        }

        if (maxBurn > 0) {
            if (currentBurn >= maxBurn) {
                maxBurn = 0;
                needUpdate = true;
            } else {
                return;
            }
        }

        if (!needUpdate) {
            return;
        }

        currentBurn = 0;
        if (stopped) {
            stopped = false;
            return;
        }

        var machine = machine();
        if (machine.isEmpty()) {
            return;
        }
        var machine1 = machine.get();

        var maxParallel = burnParallel();
        burningItem = ItemStack.EMPTY;
        for (var stack : fuelPort.getAllItems()) {
            if (ForgeHooks.getBurnTime(stack, null) > 0) {
                var stack1 = StackHelper.copyWithCount(stack, maxParallel);
                var extracted = fuelPort.extractItem(stack1, false);
                if (!extracted.isEmpty()) {
                    MetricsManager.reportItem("item_consumed", machine1, extracted);
                    maxBurn = ForgeHooks.getBurnTime(extracted, null) * PROGRESS_PER_TICK;
                    burningItem = extracted;
                    break;
                }
            }
        }

        needUpdate = false;
        setChanged();
    }

    @Override
    public void onWorkTick(double partial) {
        var machine = machine();
        if (machine.isEmpty()) {
            return;
        }
        var machine1 = machine.get();

        var heatInput = 0d;
        if (maxBurn > 0) {
            currentBurn = Math.min(maxBurn, currentBurn + (long) (burnSpeed * (double) PROGRESS_PER_TICK));
            heatInput = burnHeat * burningItem.getCount();
        }

        var world = machine1.world();
        tick(world, heatInput, boilParallel(), (input, output) -> {
            MetricsManager.reportFluid("fluid_consumed", machine1, input);
            MetricsManager.reportFluid("fluid_produced", machine1, output);
        });

        setChanged();
    }

    @Override
    public double maxHeat() {
        return 600d;
    }

    @Override
    public long progressTicks() {
        return (long) Math.floor((double) currentBurn / PROGRESS_PER_TICK / burnSpeed);
    }

    @Override
    public long maxProgressTicks() {
        return (long) Math.ceil((double) maxBurn / PROGRESS_PER_TICK / burnSpeed);
    }

    @Override
    public Optional<IProcessingObject> getInfo(int port, int index) {
        if (index > 0) {
            return Optional.empty();
        }
        return switch (port) {
            case 0 -> Optional.of(new ProcessingIngredients.ItemIngredient(burningItem));
            case 1 -> inputInfo();
            case 2 -> outputInfo();
            default -> Optional.empty();
        };
    }

    @Override
    public List<IProcessingObject> getAllInfo() {
        var ret = new ArrayList<IProcessingObject>();
        ret.add(new ProcessingIngredients.ItemIngredient(burningItem));
        addAllInfo(ret::add);
        return ret;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = super.serializeNBT();
        tag.putLong("maxBurn", maxBurn);
        tag.putLong("currentBurn", currentBurn);
        tag.put("burningItem", burningItem.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        maxBurn = tag.getLong("maxBurn");
        currentBurn = tag.getLong("currentBurn");
        burningItem = ItemStack.of(tag.getCompound("burningItem"));
    }
}
