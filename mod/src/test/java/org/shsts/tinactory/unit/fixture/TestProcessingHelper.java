package org.shsts.tinactory.unit.fixture;

import com.mojang.serialization.Codec;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.recipe.ProcessingInfo;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.StackIngredient;
import org.shsts.tinactory.core.recipe.StackResult;

public final class TestProcessingHelper {
    private TestProcessingHelper() {}

    public static final Codec<IProcessingIngredient> INGREDIENT_CODEC =
        Codec.STRING.dispatch(IProcessingObject::codecName, name -> switch (name) {
            case "test_ingredient" -> TestIngredient.CODEC;
            default -> throw new IllegalArgumentException("Unknown ingredient codec: " + name);
        });

    public static final Codec<IProcessingResult> RESULT_CODEC =
        Codec.STRING.dispatch(IProcessingObject::codecName, name -> switch (name) {
            case "test_result" -> TestResult.CODEC;
            default -> throw new IllegalArgumentException("Unknown result codec: " + name);
        });

    public static final Codec<IProcessingObject> OBJECT_CODEC =
        Codec.STRING.dispatch(IProcessingObject::codecName, name -> switch (name) {
            case "test_ingredient" -> TestIngredient.CODEC;
            case "test_result" -> TestResult.CODEC;
            default -> throw new IllegalArgumentException("Unknown object codec: " + name);
        });

    public static final Codec<ProcessingInfo> INFO_CODEC = ProcessingInfo.codec(OBJECT_CODEC);
    public static final Codec<ProcessingRecipe.Input> INPUT_CODEC = ProcessingRecipe.inputCodec(INGREDIENT_CODEC);
    public static final Codec<ProcessingRecipe.Output> OUTPUT_CODEC = ProcessingRecipe.outputCodec(RESULT_CODEC);

    public static ProcessingRecipe.Input input(int port, String key, int amount) {
        return new ProcessingRecipe.Input(port, new TestIngredient(key, amount));
    }

    public static ProcessingRecipe.Output output(int port, String key, int amount) {
        return new ProcessingRecipe.Output(port, new TestResult(key, amount));
    }

    public static ProcessingRecipe.Input inputStack(int port, String key, int amount) {
        return new ProcessingRecipe.Input(port, new StackIngredient<>("test_stack_ingredient",
            PortType.ITEM, TestStack.item(key, amount), TestStack.ADAPTER));
    }

    public static ProcessingRecipe.Output outputStack(int port, String key, int amount) {
        return new ProcessingRecipe.Output(port, new StackResult<>("test_stack_result",
            PortType.ITEM, 1d, TestStack.item(key, amount), TestStack.ADAPTER));
    }
}
