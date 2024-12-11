package org.shsts.tinactory.registrate.builder;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.IForgeRegistry;
import org.shsts.tinactory.core.common.SmartRecipe;
import org.shsts.tinactory.core.common.SmartRecipeSerializer;
import org.shsts.tinactory.core.common.Transformer1;
import org.shsts.tinactory.core.common.XBuilderBase;
import org.shsts.tinactory.core.recipe.SmartRecipeBuilder;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;
import org.slf4j.Logger;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RecipeTypeBuilder<T extends SmartRecipe<?>, B extends XBuilderBase<?, ?, B>, P>
    extends EntryBuilder<RecipeType<T>, RecipeTypeEntry<T, B>, P, RecipeTypeBuilder<T, B, P>> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final SmartRecipeSerializer.Factory<T, B> serializerFactory;
    @Nullable
    private SmartRecipeBuilder.Factory<T, B> builderFactory = null;
    @Nullable
    private Class<T> clazz;
    private Transformer1<B> defaults = $ -> $;

    public RecipeTypeBuilder(Registrate registrate, String id, P parent,
        SmartRecipeSerializer.Factory<T, B> serializerFactory) {
        super(registrate, id, parent);
        this.serializerFactory = serializerFactory;
    }

    public RecipeTypeBuilder<T, B, P> builder(SmartRecipeBuilder.Factory<T, B> factory) {
        builderFactory = factory;
        return this;
    }

    public RecipeTypeBuilder<T, B, P> clazz(Class<T> value) {
        clazz = value;
        return this;
    }

    public RecipeTypeBuilder<T, B, P> defaults(Transformer1<B> trans) {
        defaults = defaults.chain(trans);
        return this;
    }

    public void registerSerializer(IForgeRegistry<RecipeSerializer<?>> registry) {
        LOGGER.trace("register object {} {}", registry.getRegistryName(), loc);
        assert entry != null;
        var serializer = serializerFactory.create(entry);
        serializer.setRegistryName(loc);
        registry.register(serializer);
        entry.setSerializer(serializer);
    }

    @Override
    protected RecipeType<T> createObject() {
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
        return id;
    }

    public Class<T> getClazz() {
        assert clazz != null;
        return clazz;
    }

    public Transformer1<B> getDefaults() {
        return defaults;
    }

    @Override
    protected RecipeTypeEntry<T, B> createEntry() {
        return registrate.recipeTypeHandler.register(this);
    }
}
