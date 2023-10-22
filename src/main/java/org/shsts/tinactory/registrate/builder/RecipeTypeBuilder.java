package org.shsts.tinactory.registrate.builder;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.IForgeRegistry;
import org.shsts.tinactory.core.SmartRecipe;
import org.shsts.tinactory.core.SmartRecipeSerializer;
import org.shsts.tinactory.registrate.RecipeTypeEntry;
import org.shsts.tinactory.registrate.Registrate;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RecipeTypeBuilder<T extends SmartRecipe<?, T>, B extends SmartRecipeBuilder<T, B>,
        S extends SmartRecipeSerializer<T, B>, P>
        extends EntryBuilder<RecipeType<T>, RecipeTypeEntry<T, B>, P, RecipeTypeBuilder<T, B, S, P>> {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final SmartRecipeSerializer.Factory<T, B, S> serializer;
    @Nullable
    protected Supplier<RecipeType<? super T>> existingType = null;
    @Nullable
    protected SmartRecipeBuilder.Factory<T, B> builderFactory = null;

    public RecipeTypeBuilder(Registrate registrate, String id, P parent,
                             SmartRecipeSerializer.Factory<T, B, S> serializer) {
        super(registrate, id, parent);
        this.serializer = serializer;
    }

    public RecipeTypeBuilder<T, B, S, P> existingType(Supplier<RecipeType<? super T>> existingType) {
        this.existingType = existingType;
        return self();
    }

    public RecipeTypeBuilder<T, B, S, P> builder(SmartRecipeBuilder.Factory<T, B> factory) {
        this.builderFactory = factory;
        return self();
    }

    public void registerSerializer(IForgeRegistry<RecipeSerializer<?>> registry) {
        LOGGER.debug("register object {} {}", registry.getRegistryName(), this.loc);
        assert this.entry != null;
        var serializer = this.serializer.create(this.entry);
        serializer.setRegistryName(this.loc);
        registry.register(serializer);
        this.entry.setSerializer(serializer);
    }

    @Override
    public RecipeType<T> createObject() {
        var loc = this.loc.toString();
        return new RecipeType<>() {
            @Override
            public String toString() {
                return loc;
            }
        };
    }

    public boolean willCreateType() {
        return this.existingType == null;
    }

    public Supplier<RecipeType<? super T>> getExistingType() {
        assert this.existingType != null;
        return this.existingType;
    }

    public SmartRecipeBuilder.Factory<T, B> getBuilderFactory() {
        assert this.builderFactory != null;
        return this.builderFactory;
    }

    @Override
    protected RecipeTypeEntry<T, B> createEntry() {
        return this.registrate.recipeTypeHandler.register(this);
    }
}
