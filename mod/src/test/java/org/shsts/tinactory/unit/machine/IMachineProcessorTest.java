package org.shsts.tinactory.unit.machine;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.machine.IMachineProcessor;
import org.shsts.tinactory.api.recipe.IProcessingObject;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

class IMachineProcessorTest {
    @Test
    void getProgressShouldReturnZeroWhenMaxProgressIsNonPositiveAndNoProgress() {
        var processor = new TestProcessor(0, 0);

        assertEquals(0d, processor.getProgress());
    }

    @Test
    void getProgressShouldReturnOneWhenMaxProgressIsNonPositiveAndProgressExists() {
        var processor = new TestProcessor(5, 0);

        assertEquals(1d, processor.getProgress());
    }

    @Test
    void getProgressShouldReturnRatioForPositiveMaxProgress() {
        var processor = new TestProcessor(3, 8);

        assertEquals(0.375d, processor.getProgress());
    }

    @Test
    void allowTargetRecipeShouldDefaultToFalse() {
        var processor = new TestProcessor(0, 0);

        assertFalse(processor.allowTargetRecipe(modLoc("recipe")));
    }

    private record TestProcessor(long progressTicks, long maxProgressTicks) implements IMachineProcessor {
        @Override
        public Optional<IProcessingObject> getInfo(int port, int index) {
            return Optional.empty();
        }

        @Override
        public List<IProcessingObject> getAllInfo() {
            return List.of();
        }

        @Override
        public double workSpeed() {
            return 0d;
        }

        @Override
        public void onPreWork() {}

        @Override
        public void onWorkTick(double partial) {}

        @Override
        public boolean isWorking(double partial) {
            return false;
        }

        @Override
        public boolean supportsRecipeType(ResourceLocation recipeTypeId) {
            return false;
        }
    }
}
