package org.shsts.tinactory.unit.machine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.gui.IRenderDescriptor;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.gui.EmptyRenderDescriptor;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.client.IRecipeBookItem;
import org.shsts.tinactory.core.machine.IRecipeProcessor;
import org.shsts.tinactory.core.machine.ProcessingRuntime;
import org.shsts.tinactory.core.recipe.ProcessingInfo;
import org.shsts.tinactory.unit.fixture.TestContainer;
import org.shsts.tinactory.unit.fixture.TestIngredient;
import org.shsts.tinactory.unit.fixture.TestMachine;
import org.shsts.tinactory.unit.fixture.TestProcessingObject;
import org.shsts.tinactory.unit.fixture.TestRecipeManager.TestEntry;
import org.shsts.tinactory.unit.fixture.TestResult;
import org.shsts.tinycorelib.api.core.DistLazy;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessingRuntimeTest {
    private static final ResourceLocation PROCESSING_TYPE = new ResourceLocation("tinactory", "test_processing");
    private static final ResourceLocation RECIPE_ID = new ResourceLocation("tinactory", "runtime_recipe");

    @Test
    void shouldAggregateRecipeBookItemsAcrossProcessors() {
        var machine = new TestMachine(new TestContainer());
        var runtime = runtime(machine,
            new TestRecipeProcessor().recipeBookItem("alpha"),
            new TestRecipeProcessor().recipeBookItem("beta"));

        var items = runtime.recipeBookItems().getValue();

        assertEquals(List.of("tinactory:alpha", "tinactory:beta"),
            items.stream().map(item -> item.loc().toString()).toList());
    }

    @Test
    void shouldStartProgressAndFinishRecipesUsingMachineParallel() {
        var machine = new TestMachine(new TestContainer()).parallel(3);
        var processor = new TestRecipeProcessor()
            .recipe(RECIPE_ID)
            .progressPerTick(5)
            .maxProgress(10)
            .inputInfo(new ProcessingInfo(0, new TestIngredient("ore", 2)))
            .doneResult(new TestResult("dust", 1));
        var runtime = runtime(machine, processor);

        runtime.onPreWork();
        runtime.onWorkTick(1d);
        runtime.onWorkTick(1d);

        assertEquals(3, processor.beginParallel());
        assertEquals(10L, runtime.progressTicks());
        assertEquals(10L, runtime.maxProgressTicks());
        assertEquals(List.of(new TestIngredient("ore", 2)), runtime.getAllInfo());
        assertEquals(List.of(new TestResult("dust", 1)), processor.doneResults());
        assertTrue(runtime.isWorking(1d));
    }

    @Test
    void shouldReportConsumedAndProducedObjectsThroughCallback() {
        var reportedObjects = new ArrayList<Report>();
        var machine = new TestMachine(new TestContainer());
        var ingredient = new TestIngredient("ore", 2);
        var result = new TestResult("dust", 1);
        var processor = new TestRecipeProcessor()
            .recipe(RECIPE_ID)
            .inputInfo(new ProcessingInfo(0, ingredient))
            .doneResult(result);
        var runtime = runtimeBuilder(machine, processor)
            .onReportObject((direction, object) -> reportedObjects.add(new Report(direction, object)))
            .build();

        runtime.onPreWork();
        runtime.onWorkTick(1d);

        assertEquals(List.of(
            new Report(PortDirection.INPUT, ingredient),
            new Report(PortDirection.OUTPUT, result)), reportedObjects);
    }

    @Test
    void shouldRecoverSerializedStateAndContinueRecipe() {
        var machine = new TestMachine(new TestContainer()).targetRecipe(RECIPE_ID);
        var originalProcessor = new TestRecipeProcessor()
            .recipe(RECIPE_ID)
            .inputInfo(new ProcessingInfo(2, new TestIngredient("ore", 1)))
            .progressPerTick(4)
            .maxProgress(12);
        var original = runtime(machine, originalProcessor);
        original.onPreWork();
        original.onWorkTick(1d);
        var saved = original.serializeNBT();

        var restoredProcessor = new TestRecipeProcessor()
            .recipe(RECIPE_ID)
            .inputInfo(new ProcessingInfo(2, new TestIngredient("ore", 1)))
            .progressPerTick(4)
            .maxProgress(12);
        var restored = runtime(machine, restoredProcessor);
        restored.deserializeNBT(saved);
        restored.onPreWork();

        assertTrue(restoredProcessor.continued());
        assertEquals(4L, restored.progressTicks());
        assertEquals(List.of(new TestIngredient("ore", 1)), restored.getAllInfo());
    }

    @Test
    void shouldApplyTargetRecipeThroughMachineConfigAndSignalUpdates() {
        var updates = new AtomicInteger();
        var machine = new TestMachine(new TestContainer()).targetRecipe(RECIPE_ID);
        var processor = new TestRecipeProcessor().recipe(RECIPE_ID);
        var runtime = runtimeBuilder(machine, processor)
            .autoRecipe(false)
            .onUpdate(updates::incrementAndGet)
            .build();

        runtime.onPreWork();

        assertEquals(Optional.of(RECIPE_ID), processor.targetRecipe());
        assertEquals(1, updates.get());
    }

    @Test
    void shouldNotStartRecipeWhenAutoRecipeIsDisabledWithoutTarget() {
        var updates = new AtomicInteger();
        var machine = new TestMachine(new TestContainer());
        var processor = new TestRecipeProcessor().recipe(RECIPE_ID);
        var runtime = runtimeBuilder(machine, processor)
            .autoRecipe(false)
            .onUpdate(updates::incrementAndGet)
            .build();

        runtime.onPreWork();

        assertEquals(0, processor.beginParallel());
        assertEquals(0L, runtime.progressTicks());
        assertFalse(runtime.isWorking(1d));
        assertEquals(1, updates.get());
    }

    @Test
    void shouldShortCircuitPreWorkWhenStopped() {
        var updates = new AtomicInteger();
        var machine = new TestMachine(new TestContainer());
        var processor = new TestRecipeProcessor().recipe(RECIPE_ID);
        var runtime = runtimeBuilder(machine, processor)
            .onUpdate(updates::incrementAndGet)
            .build();

        runtime.setStopped(true);

        runtime.onPreWork();

        assertEquals(0L, runtime.progressTicks());
        assertEquals(0, updates.get());
    }

    @Test
    void shouldIgnorePreWorkAndTicksWhenMachineIsMissing() {
        var machineRef = new AtomicReference<Optional<IMachine>>(Optional.of(new TestMachine(new TestContainer())));
        var updates = new AtomicInteger();
        var processor = new TestRecipeProcessor().recipe(RECIPE_ID).maxProgress(10).progressPerTick(3);
        var runtime = runtimeBuilder(processor)
            .machineSupplier(machineRef::get)
            .autoRecipe(true)
            .onUpdate(updates::incrementAndGet)
            .build();

        runtime.onPreWork();
        machineRef.set(Optional.empty());
        runtime.onWorkTick(1d);

        assertEquals(0L, runtime.progressTicks());
        assertEquals(1, updates.get());

        var emptyRuntime = runtimeBuilder(new TestRecipeProcessor().recipe(RECIPE_ID))
            .machineSupplier(Optional::empty)
            .autoRecipe(true)
            .onUpdate(updates::incrementAndGet)
            .build();
        emptyRuntime.onPreWork();
        assertEquals(1, updates.get());
    }

    @Test
    void shouldClearCompletedRecipeStateBeforeLookingForNextWork() {
        var machine = new TestMachine(new TestContainer());
        var processor = new TestRecipeProcessor()
            .recipe(RECIPE_ID)
            .inputInfo(new ProcessingInfo(0, new TestIngredient("ore", 1)))
            .progressPerTick(1)
            .maxProgress(1);
        var runtime = runtime(machine, processor);

        runtime.onPreWork();
        runtime.onWorkTick(1d);
        processor.noRecipe();
        runtime.onPreWork();

        assertTrue(runtime.getAllInfo().isEmpty());
        assertEquals(0L, runtime.progressTicks());
    }

    @Test
    void shouldExposeIdleAndActiveRuntimeMachineState() {
        var machine = new TestMachine(new TestContainer());
        var runtime = runtime(machine, new TestRecipeProcessor()
            .recipe(RECIPE_ID)
            .progressPerTick(1)
            .maxProgress(10)
            .machineType(ElectricMachineType.GENERATOR)
            .powerGen(7.5d)
            .powerCons(2.5d));

        assertEquals(-1d, runtime.workSpeed());
        assertFalse(runtime.isWorking(1d));
        assertEquals(ElectricMachineType.NONE, runtime.machineType());
        assertEquals(0d, runtime.powerGen());
        assertEquals(0d, runtime.powerCons());

        runtime.onPreWork();
        runtime.onWorkTick(0.5d);

        assertEquals(0.5d, runtime.workSpeed());
        assertTrue(runtime.isWorking(1d));
        assertEquals(ElectricMachineType.GENERATOR, runtime.machineType());
        assertEquals(7.5d, runtime.powerGen());
        assertEquals(2.5d, runtime.powerCons());
    }

    @Test
    void shouldSupportKnownRecipeTypesOnly() {
        var runtime = runtime(new TestMachine(new TestContainer()), new TestRecipeProcessor().recipe(RECIPE_ID));

        assertTrue(runtime.supportsRecipeType(PROCESSING_TYPE));
        assertFalse(runtime.supportsRecipeType(new ResourceLocation("tinactory", "other_processing")));
    }

    @Test
    void shouldDelegateTargetRecipeAcceptanceToRecipeProcessors() {
        var runtime = runtime(new TestMachine(new TestContainer()), new TestRecipeProcessor().recipe(RECIPE_ID));

        assertTrue(runtime.allowTargetRecipe(RECIPE_ID));
        assertFalse(runtime.allowTargetRecipe(new ResourceLocation("tinactory", "other_recipe")));
    }

    @Test
    void shouldTreatEmptySerializationTagAsIdleState() {
        var runtime = runtime(new TestMachine(new TestContainer()), new TestRecipeProcessor().recipe(RECIPE_ID));

        runtime.deserializeNBT(new CompoundTag());

        assertEquals(0L, runtime.progressTicks());
        assertTrue(runtime.getAllInfo().isEmpty());
        assertTrue(runtime.serializeNBT().isEmpty());
    }

    private static final class RuntimeBuilder {
        private final List<IRecipeProcessor<?>> processors;
        private Supplier<Optional<IMachine>> machineSupplier;
        private boolean autoRecipe = true;
        private Runnable onUpdate = () -> {};
        private BiConsumer<PortDirection, IProcessingObject> onReportObject = (dir, obj) -> {};

        private RuntimeBuilder(List<IRecipeProcessor<?>> processors) {
            this.processors = processors;
        }

        public RuntimeBuilder machineSupplier(Supplier<Optional<IMachine>> val) {
            machineSupplier = val;
            return this;
        }

        public RuntimeBuilder autoRecipe(boolean val) {
            autoRecipe = val;
            return this;
        }

        public RuntimeBuilder onUpdate(Runnable val) {
            onUpdate = val;
            return this;
        }

        public RuntimeBuilder onReportObject(BiConsumer<PortDirection, IProcessingObject> val) {
            onReportObject = val;
            return this;
        }

        public ProcessingRuntime build() {
            var properties = new ProcessingRuntime.Properties(
                processors, autoRecipe, machineSupplier,
                () -> false, onUpdate, onReportObject,
                TestProcessingObject.INFO_CODEC);
            return new ProcessingRuntime(properties);
        }
    }

    private static RuntimeBuilder runtimeBuilder(TestRecipeProcessor... processors) {
        return new RuntimeBuilder(Arrays.asList(processors));
    }

    private static RuntimeBuilder runtimeBuilder(TestMachine machine, TestRecipeProcessor... processors) {
        return runtimeBuilder(processors).machineSupplier(() -> Optional.of(machine));
    }

    private static ProcessingRuntime runtime(TestMachine machine, TestRecipeProcessor... processors) {
        return runtimeBuilder(machine, processors).build();
    }

    private static final class TestRecipeProcessor implements IRecipeProcessor<ResourceLocation> {
        private final List<TestRecipeBookItem> recipeBookItems = new ArrayList<>();
        private final List<IProcessingResult> doneResults = new ArrayList<>();
        private Optional<ResourceLocation> recipe = Optional.empty();
        private Optional<ResourceLocation> targetRecipe = Optional.empty();
        private List<ProcessingInfo> inputInfo = List.of();
        private List<IProcessingResult> results = List.of();
        private long progressPerTick = 1L;
        private long maxProgress = 1L;
        private int beginParallel;
        private boolean continued;
        private ElectricMachineType machineType = ElectricMachineType.NONE;
        private double powerGen;
        private double powerCons;

        private TestRecipeProcessor recipe(ResourceLocation value) {
            recipe = Optional.of(value);
            return this;
        }

        private TestRecipeProcessor noRecipe() {
            recipe = Optional.empty();
            return this;
        }

        private TestRecipeProcessor recipeBookItem(String path) {
            recipeBookItems.add(new TestRecipeBookItem(new ResourceLocation("tinactory", path)));
            return this;
        }

        private TestRecipeProcessor inputInfo(ProcessingInfo... values) {
            inputInfo = List.of(values);
            return this;
        }

        private TestRecipeProcessor doneResult(IProcessingResult... values) {
            results = List.of(values);
            return this;
        }

        private TestRecipeProcessor progressPerTick(long value) {
            progressPerTick = value;
            return this;
        }

        private TestRecipeProcessor maxProgress(long value) {
            maxProgress = value;
            return this;
        }

        private TestRecipeProcessor machineType(ElectricMachineType value) {
            machineType = value;
            return this;
        }

        private TestRecipeProcessor powerGen(double value) {
            powerGen = value;
            return this;
        }

        private TestRecipeProcessor powerCons(double value) {
            powerCons = value;
            return this;
        }

        private int beginParallel() {
            return beginParallel;
        }

        private List<IProcessingResult> doneResults() {
            return doneResults;
        }

        private boolean continued() {
            return continued;
        }

        private Optional<ResourceLocation> targetRecipe() {
            return targetRecipe;
        }

        @Override
        public ResourceLocation recipeTypeId() {
            return PROCESSING_TYPE;
        }

        @Override
        public Class<ResourceLocation> baseClass() {
            return ResourceLocation.class;
        }

        @Override
        public Optional<IEntry<ResourceLocation>> byLoc(ResourceLocation loc) {
            return recipe.filter(loc::equals).map($ -> new TestEntry<>($, $));
        }

        @Override
        public DistLazy<List<IRecipeBookItem>> recipeBookItems(IMachine machine) {
            return () -> () -> List.copyOf(recipeBookItems);
        }

        @Override
        public boolean allowTargetRecipe(boolean isClientSide, ResourceLocation loc, IMachine machine) {
            return recipe.filter(loc::equals).isPresent();
        }

        @Override
        public void setTargetRecipe(ResourceLocation loc, IMachine machine) {
            targetRecipe = Optional.of(loc);
        }

        @Override
        public Optional<IEntry<ResourceLocation>> newRecipe(IMachine machine) {
            return recipe.map($ -> new TestEntry<>($, $));
        }

        @Override
        public Optional<IEntry<ResourceLocation>> newRecipe(IMachine machine, ResourceLocation target) {
            return recipe.filter(target::equals).map($ -> new TestEntry<>($, $));
        }

        @Override
        public void onWorkBegin(ResourceLocation recipe, IMachine machine, int maxParallel,
            Consumer<ProcessingInfo> callback) {
            beginParallel = maxParallel;
            inputInfo.forEach(callback);
        }

        @Override
        public void onWorkContinue(ResourceLocation recipe, IMachine machine) {
            continued = true;
        }

        @Override
        public long onWorkProgress(ResourceLocation recipe, double partial) {
            return progressPerTick;
        }

        @Override
        public void onWorkDone(ResourceLocation recipe, IMachine machine, Random random,
            Consumer<IProcessingResult> callback) {
            results.forEach(result -> {
                doneResults.add(result);
                callback.accept(result);
            });
        }

        @Override
        public long maxWorkProgress(ResourceLocation recipe) {
            return maxProgress;
        }

        @Override
        public long workTicksFromProgress(long progress) {
            return progress;
        }

        @Override
        public double workSpeed(double partial) {
            return partial;
        }

        @Override
        public ElectricMachineType electricMachineType(ResourceLocation recipe) {
            return machineType;
        }

        @Override
        public double powerGen(ResourceLocation recipe) {
            return powerGen;
        }

        @Override
        public double powerCons(ResourceLocation recipe) {
            return powerCons;
        }

        @Override
        public CompoundTag serializeNBT() {
            return new CompoundTag();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
        }
    }

    private record Report(PortDirection direction, IProcessingObject object) {
    }

    private record TestRecipeBookItem(ResourceLocation loc) implements IRecipeBookItem {
        @Override
        public boolean isMarker() {
            return false;
        }

        @Override
        public void select(Layout layout, BiConsumer<Layout.SlotInfo, IProcessingObject> ingredientCons) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<List<Component>> buttonToolTip() {
            return Optional.empty();
        }

        @Override
        public IRenderDescriptor display() {
            return EmptyRenderDescriptor.INSTANCE;
        }
    }
}
