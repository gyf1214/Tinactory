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

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RecipeTypeBuilder<T extends SmartRecipe<?, T>, B, S extends SmartRecipeSerializer<T, B>, P>
        extends EntryBuilder<RecipeType<T>, RecipeTypeEntry<T, B>, P, RecipeTypeBuilder<T, B, S, P>> {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final SmartRecipeSerializer.Factory<T, B, S> serializer;
    @Nullable
    protected SmartRecipeBuilder.Factory<T, B> builderFactory = null;
    protected String prefix = "";

    public RecipeTypeBuilder(Registrate registrate, String id, P parent,
                             SmartRecipeSerializer.Factory<T, B, S> serializer) {
        super(registrate, id, parent);
        this.serializer = serializer;
    }

    public RecipeTypeBuilder<T, B, S, P> builder(SmartRecipeBuilder.Factory<T, B> factory) {
        this.builderFactory = factory;
        return self();
    }

    public RecipeTypeBuilder<T, B, S, P> prefix(String prefix) {
        this.prefix = prefix;
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

    public SmartRecipeBuilder.Factory<T, B> getBuilderFactory() {
        assert this.builderFactory != null;
        return this.builderFactory;
    }

    public String getPrefix() {
        return this.prefix;
    }

    @Override
    protected RecipeTypeEntry<T, B> createEntry() {
        return this.registrate.recipeTypeHandler.register(this);
    }
}
