package org.shsts.tinactory.registrate.handler;

import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import org.shsts.tinactory.core.SmartRecipe;
import org.shsts.tinactory.core.SmartRecipeSerializer;
import org.shsts.tinactory.registrate.RecipeTypeEntry;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.builder.RecipeTypeBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class RecipeTypeHandler {
    private final Registrate registrate;
    private final List<RecipeTypeBuilder<?, ?, ?, ?>> builders = new ArrayList<>();
    private final DeferredRegister<RecipeType<?>> recipeTypeRegister;

    public RecipeTypeHandler(Registrate registrate) {
        this.registrate = registrate;
        this.recipeTypeRegister = DeferredRegister.create(Registry.RECIPE_TYPE_REGISTRY, registrate.modid);
    }

    public <T extends SmartRecipe<?, T>, B, S extends SmartRecipeSerializer<T, B>>
    RecipeTypeEntry<T, B> register(RecipeTypeBuilder<T, B, S, ?> builder) {
        this.builders.add(builder);
        var recipeType = this.recipeTypeRegister.register(builder.id, builder::buildObject);
        return new RecipeTypeEntry<>(registrate, builder.id, recipeType,
                builder.getBuilderFactory(), builder.getPrefix());
    }

    public void onRegisterSerializer(RegistryEvent.Register<RecipeSerializer<?>> event) {
        for (var builder : builders) {
            builder.registerSerializer(event.getRegistry());
        }
        this.builders.clear();
    }

    public void addListeners(IEventBus modEventBus) {
        modEventBus.addGenericListener(RecipeSerializer.class, this::onRegisterSerializer);
        this.recipeTypeRegister.register(modEventBus);
    }
}
