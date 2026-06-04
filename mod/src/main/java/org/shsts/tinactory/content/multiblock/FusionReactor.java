package org.shsts.tinactory.content.multiblock;

import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.client.IRecipeBookItem;
import org.shsts.tinactory.core.machine.IRecipeProcessor;
import org.shsts.tinactory.core.machine.ProcessingRuntime;
import org.shsts.tinactory.core.recipe.ProcessingInfo;
import org.shsts.tinactory.integration.metrics.MetricsManager;
import org.shsts.tinactory.integration.multiblock.Multiblock;
import org.shsts.tinactory.integration.multiblock.MultiblockInterface;
import org.shsts.tinactory.integration.recipe.ProcessingHelper;
import org.shsts.tinycorelib.api.core.DistLazy;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FusionReactor extends ProcessingRuntime {
    private static final double STARTUP_EPSILON = 1d;
    private final State state;
    private final BlockEntity blockEntity;

    public FusionReactor(BlockEntity blockEntity,
        Collection<Function<BlockEntity, ? extends IRecipeProcessor<?>>> processorFactories,
        boolean autoRecipe, Properties properties) {
        this(blockEntity, processorFactories, autoRecipe, new State(properties));
    }

    private FusionReactor(BlockEntity blockEntity,
        Collection<Function<BlockEntity, ? extends IRecipeProcessor<?>>> processorFactories,
        boolean autoRecipe, State state) {
        super(wrap(blockEntity, processorFactories, state), autoRecipe,
            () -> machine(blockEntity),
            () -> Objects.requireNonNull(blockEntity.getLevel()).isClientSide,
            blockEntity::setChanged,
            (direction, object) -> reportProcessingObject(blockEntity, direction, object),
            ProcessingHelper.INFO_CODEC);
        this.blockEntity = blockEntity;
        this.state = state;
        state.owner = this;
    }

    private static Collection<IRecipeProcessor<?>> wrap(BlockEntity blockEntity,
        Collection<Function<BlockEntity, ? extends IRecipeProcessor<?>>> processorFactories, State state) {
        return processorFactories.stream()
            .<IRecipeProcessor<?>>map(factory -> new StartupProcessor<>(factory.apply(blockEntity), state))
            .toList();
    }

    private static Optional<IMachine> machine(BlockEntity blockEntity) {
        return Multiblock.get(blockEntity).getInterface().map($ -> $);
    }

    private static void reportProcessingObject(BlockEntity blockEntity, PortDirection direction,
        IProcessingObject object) {
        var action = switch (direction) {
            case INPUT -> "consumed";
            case OUTPUT -> "produced";
            case NONE -> throw new IllegalArgumentException("unexpected processing direction: " + direction);
        };
        machine(blockEntity).ifPresent(machine -> MetricsManager.reportProcessingObject(action, machine, object));
    }

    private Optional<Voltage> voltage() {
        return machine(blockEntity)
            .filter(MultiblockInterface.class::isInstance)
            .map(MultiblockInterface.class::cast)
            .map($ -> $.voltage);
    }

    private double startupCapacity() {
        return voltage()
            .map($ -> Math.scalb((double) state.properties.startEnergyFactor, $.rank))
            .orElse(0d);
    }

    private double chargeRate() {
        return voltage()
            .map($ -> (double) $.value * state.properties.chargeAmperage)
            .orElse(0d);
    }

    private void setChanged() {
        blockEntity.setChanged();
    }

    @Override
    public void onPreWork() {
        state.beginPreWork();
        if (!state.isFull()) {
            onContainerChange();
        }
        super.onPreWork();
        state.finishPreWork();
    }

    @Override
    public void onWorkTick(double partial) {
        if (state.charging) {
            var before = state.startupEnergy;
            state.startupEnergy = Math.min(startupCapacity(), state.startupEnergy + chargeRate() * partial);
            if (state.startupEnergy != before) {
                setChanged();
            }
            if (state.isFull()) {
                onContainerChange();
            }
        } else if (!state.running && !state.possible) {
            var before = state.startupEnergy;
            state.startupEnergy *= 1d - state.properties.decayRate;
            if (state.startupEnergy < STARTUP_EPSILON) {
                state.startupEnergy = 0d;
            }
            if (state.startupEnergy != before) {
                setChanged();
            }
        }
        super.onWorkTick(partial);
    }

    @Override
    public boolean isWorking(double partial) {
        return state.charging || super.isWorking(partial);
    }

    @Override
    public ElectricMachineType machineType() {
        return state.charging ? ElectricMachineType.CONSUMER : super.machineType();
    }

    @Override
    public double powerCons() {
        if (!state.charging) {
            return super.powerCons();
        }
        return Math.min(chargeRate(), Math.max(0d, startupCapacity() - state.startupEnergy));
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = super.serializeNBT();
        tag.putDouble("startupEnergy", state.startupEnergy);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        state.startupEnergy = tag.getDouble("startupEnergy");
    }

    public record Properties(long startEnergyFactor, double chargeAmperage, double decayRate) {
        public static Properties fromJson(JsonObject jo) {
            return new Properties(
                GsonHelper.getAsLong(jo, "startEnergyFactor"),
                GsonHelper.getAsDouble(jo, "chargeAmperage"),
                GsonHelper.getAsDouble(jo, "decayRate"));
        }
    }

    private static class State {
        private final Properties properties;
        private FusionReactor owner;
        private double startupEnergy = 0d;
        private boolean possible = false;
        private boolean running = false;
        private boolean charging = false;

        private State(Properties properties) {
            this.properties = properties;
        }

        private boolean isFull() {
            return startupEnergy >= owner.startupCapacity();
        }

        private void beginPreWork() {
            possible = false;
            charging = false;
        }

        private void finishPreWork() {
            charging = !isFull() && (running || possible);
        }

        private void beginWork() {
            running = true;
            startupEnergy = owner.startupCapacity();
            charging = false;
            owner.setChanged();
        }

        private void finishWork() {
            running = false;
        }
    }

    private record StartupProcessor<T>(IRecipeProcessor<T> delegate, State state) implements IRecipeProcessor<T> {
        @Override
        public ResourceLocation recipeTypeId() {
            return delegate.recipeTypeId();
        }

        @Override
        public Class<T> baseClass() {
            return delegate.baseClass();
        }

        @Override
        public Optional<T> byLoc(ResourceLocation loc) {
            return delegate.byLoc(loc);
        }

        @Override
        public ResourceLocation toLoc(T recipe) {
            return delegate.toLoc(recipe);
        }

        @Override
        public DistLazy<List<IRecipeBookItem>> recipeBookItems(IMachine machine) {
            return delegate.recipeBookItems(machine);
        }

        @Override
        public boolean allowTargetRecipe(boolean isClientSide, ResourceLocation loc, IMachine machine) {
            return delegate.allowTargetRecipe(isClientSide, loc, machine);
        }

        @Override
        public void setTargetRecipe(ResourceLocation loc, IMachine machine) {
            delegate.setTargetRecipe(loc, machine);
        }

        @Override
        public Optional<T> newRecipe(IMachine machine) {
            return gate(delegate.newRecipe(machine));
        }

        @Override
        public Optional<T> newRecipe(IMachine machine, ResourceLocation target) {
            return gate(delegate.newRecipe(machine, target));
        }

        private Optional<T> gate(Optional<T> recipe) {
            state.possible = state.possible || recipe.isPresent();
            return state.isFull() ? recipe : Optional.empty();
        }

        @Override
        public void onWorkBegin(T recipe, IMachine machine, int maxParallel, Consumer<ProcessingInfo> callback) {
            state.beginWork();
            delegate.onWorkBegin(recipe, machine, maxParallel, callback);
        }

        @Override
        public void onWorkContinue(T recipe, IMachine machine) {
            state.beginWork();
            delegate.onWorkContinue(recipe, machine);
        }

        @Override
        public long onWorkProgress(T recipe, double partial) {
            return delegate.onWorkProgress(recipe, partial);
        }

        @Override
        public void onWorkDone(T recipe, IMachine machine, Random random, Consumer<IProcessingResult> callback) {
            delegate.onWorkDone(recipe, machine, random, callback);
            state.finishWork();
        }

        @Override
        public long maxWorkProgress(T recipe) {
            return delegate.maxWorkProgress(recipe);
        }

        @Override
        public long workTicksFromProgress(long progress) {
            return delegate.workTicksFromProgress(progress);
        }

        @Override
        public double workSpeed(double partial) {
            return delegate.workSpeed(partial);
        }

        @Override
        public ElectricMachineType electricMachineType(T recipe) {
            return delegate.electricMachineType(recipe);
        }

        @Override
        public double powerGen(T recipe) {
            return delegate.powerGen(recipe);
        }

        @Override
        public double powerCons(T recipe) {
            return delegate.powerCons(recipe);
        }

        @Override
        public CompoundTag serializeNBT() {
            return delegate.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            delegate.deserializeNBT(tag);
        }
    }
}
