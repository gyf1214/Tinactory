package org.shsts.tinactory.registrate;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.eventbus.api.IEventBus;
import org.shsts.tinactory.core.common.SmartRecipe;
import org.shsts.tinactory.core.common.SmartRecipeSerializer;
import org.shsts.tinactory.core.common.XBuilderBase;
import org.shsts.tinactory.registrate.builder.RecipeTypeBuilder;
import org.shsts.tinactory.registrate.handler.RecipeTypeHandler;
import org.shsts.tinactory.registrate.tracking.TrackedObjects;
import org.shsts.tinactory.registrate.tracking.TrackedType;

import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Registrate {
    public final String modid;

    // Others
    public final RecipeTypeHandler recipeTypeHandler;

    private final TrackedObjects trackedObjects;

    public Registrate(String modid) {
        this.modid = modid;

        this.recipeTypeHandler = new RecipeTypeHandler(this);

        this.trackedObjects = new TrackedObjects();
    }

    public void register(IEventBus modEventBus) {
        recipeTypeHandler.addListeners(modEventBus);
    }

    public <T extends SmartRecipe<?>,
        B extends XBuilderBase<?, ?, B>> RecipeTypeBuilder<T, B, Registrate> recipeType(
        String id, SmartRecipeSerializer.Factory<T, B> serializer) {
        return new RecipeTypeBuilder<>(this, id, this, serializer);
    }

    public <V> Map<V, String> getTracked(TrackedType<V> type) {
        return trackedObjects.getObjects(type);
    }
}
