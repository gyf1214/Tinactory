package org.shsts.tinactory.registrate.builder;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.IForgeRegistry;
import org.shsts.tinactory.core.common.SmartRecipe;
import org.shsts.tinactory.core.common.SmartRecipeSerializer;
import org.shsts.tinactory.core.common.Transformer;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RecipeTypeBuilder<T extends SmartRecipe<?, T>, B, S extends SmartRecipeSerializer<T, B>, P>
        extends EntryBuilder<RecipeType<T>, RecipeTypeEntry<T, B>, P, RecipeTypeBuilder<T, B, S, P>> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final SmartRecipeSerializer.Factory<T, B, S> serializerFactory;
    @Nullable
    private SmartRecipeBuilder.Factory<T, B> builderFactory = null;
    @Nullable
    private String prefix = null;
    @Nullable
    private Class<T> clazz;
    private Transformer<B> defaultTransformer = $ -> $;

    public RecipeTypeBuilder(Registrate registrate, String id, P parent,
                             SmartRecipeSerializer.Factory<T, B, S> serializerFactory) {
        super(registrate, id, parent);
        this.serializerFactory = serializerFactory;
    }

    public RecipeTypeBuilder<T, B, S, P> builder(SmartRecipeBuilder.Factory<T, B> factory) {
        builderFactory = factory;
        return self();
    }

    public RecipeTypeBuilder<T, B, S, P> prefix(String value) {
        prefix = value;
        return self();
    }

    public RecipeTypeBuilder<T, B, S, P> clazz(Class<T> value) {
        clazz = value;
        return self();
    }

    public RecipeTypeBuilder<T, B, S, P> builderTransform(Transformer<B> trans) {
        defaultTransformer = defaultTransformer.chain(trans);
        return this;
    }

    public void registerSerializer(IForgeRegistry<RecipeSerializer<?>> registry) {
        LOGGER.debug("register object {} {}", registry.getRegistryName(), loc);
        assert entry != null;
        var serializer = serializerFactory.create(entry);
        serializer.setRegistryName(loc);
        registry.register(serializer);
        entry.setSerializer(serializer);
    }

    @Override
    public RecipeType<T> createObject() {
        var loc1 = loc.toString();
        return new RecipeType<>() {
            @Override
            public String toString() {
                return loc1;
            }
        };
    }

    public SmartRecipeBuilder.Factory<T, B> getBuilderFactory() {
        assert builderFactory != null;
        return builderFactory;
    }

    public String getPrefix() {
        return prefix == null ? id : prefix;
    }

    public Class<T> getClazz() {
        assert clazz != null;
        return clazz;
    }

    public Transformer<B> getDefaultTransformer() {
        return defaultTransformer;
    }

    @Override
    protected RecipeTypeEntry<T, B> createEntry() {
        return registrate.recipeTypeHandler.register(this);
    }
}
