package org.shsts.tinactory.content.multiblock;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.AllCapabilities;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineProcessor;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.client.IRecipeBookItem;
import org.shsts.tinactory.core.machine.IRecipeProcessor;
import org.shsts.tinactory.core.recipe.ProcessingInfo;
import org.shsts.tinactory.integration.multiblock.MultiblockInterface;
import org.shsts.tinycorelib.api.core.DistLazy;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FusionReactor extends MultiblockProcessor {
    private static final double STARTUP_EPSILON = 1d;
    private final State state;
    private final LazyOptional<IMachineProcessor> processorCap;

    public FusionReactor(BlockEntity blockEntity,
        Collection<Function<BlockEntity, ? extends IRecipeProcessor<?>>> processorFactories,
        boolean autoRecipe, Properties properties) {
        this(blockEntity, processorFactories, autoRecipe, new State(properties));
    }

    private FusionReactor(BlockEntity blockEntity,
        Collection<Function<BlockEntity, ? extends IRecipeProcessor<?>>> processorFactories,
        boolean autoRecipe, State state) {
        super(blockEntity, wrap(processorFactories, state), autoRecipe);
        this.state = state;
        state.owner = this;
        processorCap = LazyOptional.of(FusionProcessor::new);
    }

    private static Collection<Function<BlockEntity, ? extends IRecipeProcessor<?>>> wrap(
        Collection<Function<BlockEntity, ? extends IRecipeProcessor<?>>> processorFactories, State state) {
        return processorFactories.stream()
            .<Function<BlockEntity, ? extends IRecipeProcessor<?>>>map(factory ->
                be -> new StartupProcessor<>(factory.apply(be), state))
            .toList();
    }

    private Optional<Voltage> voltage() {
        return machine()
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

    private void onPreWork() {
        state.beginPreWork();
        if (!state.isFull()) {
            runtime.onContainerChange();
        }
        runtime.onPreWork();
        state.finishPreWork();
    }

    private void onWorkTick(double partial) {
        if (state.charging) {
            var before = state.startupEnergy;
            state.startupEnergy = Math.min(startupCapacity(), state.startupEnergy + chargeRate() * partial);
            if (state.startupEnergy != before) {
                setChanged();
            }
            if (state.isFull()) {
                runtime.onContainerChange();
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
        runtime.onWorkTick(partial);
    }

    private boolean isWorking(double partial) {
        return state.charging || runtime.isWorking(partial);
    }

    @Override
    public ElectricMachineType getMachineType() {
        return state.charging ? ElectricMachineType.CONSUMER : super.getMachineType();
    }

    @Override
    public double getPowerCons() {
        if (!state.charging) {
            return super.getPowerCons();
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

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.PROCESSOR.get()) {
            return processorCap.cast();
        }
        return super.getCapability(cap, side);
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

    private class FusionProcessor implements IMachineProcessor {
        @Override
        public Optional<IProcessingObject> getInfo(int port, int index) {
            return runtime.getInfo(port, index);
        }

        @Override
        public List<IProcessingObject> getAllInfo() {
            return runtime.getAllInfo();
        }

        @Override
        public long progressTicks() {
            return runtime.progressTicks();
        }

        @Override
        public long maxProgressTicks() {
            return runtime.maxProgressTicks();
        }

        @Override
        public double workSpeed() {
            return runtime.workSpeed();
        }

        @Override
        public boolean supportsRecipeType(ResourceLocation recipeTypeId) {
            return runtime.supportsRecipeType(recipeTypeId);
        }

        @Override
        public void onPreWork() {
            FusionReactor.this.onPreWork();
        }

        @Override
        public void onWorkTick(double partial) {
            FusionReactor.this.onWorkTick(partial);
        }

        @Override
        public double getProgress() {
            return runtime.getProgress();
        }

        @Override
        public boolean isWorking(double partial) {
            return FusionReactor.this.isWorking(partial);
        }
    }
}
