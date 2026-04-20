package org.shsts.tinactory.unit.machine;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.IRecipeBookItem;
import org.shsts.tinactory.core.machine.IRecipeProcessor;
import org.shsts.tinactory.core.machine.ProcessingRuntime;
import org.shsts.tinactory.core.recipe.ProcessingInfo;
import org.shsts.tinactory.unit.fixture.TestContainer;
import org.shsts.tinactory.unit.fixture.TestIngredient;
import org.shsts.tinactory.unit.fixture.TestMachine;
import org.shsts.tinactory.unit.fixture.TestProcessingObject;
import org.shsts.tinactory.unit.fixture.TestResult;
import org.shsts.tinycorelib.api.core.DistLazy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        var runtime = new ProcessingRuntime(List.of(processor), true, () -> Optional.of(machine),
            false, () -> {}, (direction, object) -> reportedObjects.add(new Report(direction, object)),
            TestProcessingObject.INFO_CODEC);

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
        var runtime = new ProcessingRuntime(List.of(processor), false, () -> Optional.of(machine),
            false, updates::incrementAndGet, TestProcessingObject.INFO_CODEC);

        runtime.onPreWork();

        assertEquals(Optional.of(RECIPE_ID), processor.targetRecipe());
        assertEquals(1, updates.get());
    }

    private static ProcessingRuntime runtime(TestMachine machine, TestRecipeProcessor... processors) {
        return new ProcessingRuntime(List.of(processors), true, () -> Optional.of(machine),
            false, () -> {}, TestProcessingObject.INFO_CODEC);
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

        private TestRecipeProcessor recipe(ResourceLocation value) {
            recipe = Optional.of(value);
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
        public Optional<ResourceLocation> byLoc(ResourceLocation loc) {
            return recipe.filter(loc::equals);
        }

        @Override
        public ResourceLocation toLoc(ResourceLocation value) {
            return value;
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
        public Optional<ResourceLocation> newRecipe(IMachine machine) {
            return recipe;
        }

        @Override
        public Optional<ResourceLocation> newRecipe(IMachine machine, ResourceLocation target) {
            return recipe.filter(target::equals);
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
            return ElectricMachineType.NONE;
        }

        @Override
        public double powerGen(ResourceLocation recipe) {
            return 0d;
        }

        @Override
        public double powerCons(ResourceLocation recipe) {
            return 0d;
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
        public void render(PoseStack poseStack, Rect rect, int z) {
        }

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
    }
}
