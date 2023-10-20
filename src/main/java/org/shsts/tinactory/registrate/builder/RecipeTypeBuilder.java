package org.shsts.tinactory.registrate.builder;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.IForgeRegistry;
import org.shsts.tinactory.core.SmartRecipeSerializer;
import org.shsts.tinactory.registrate.RecipeTypeEntry;
import org.shsts.tinactory.registrate.Registrate;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RecipeTypeBuilder<T extends Recipe<?>, S extends SmartRecipeSerializer<T>, P>
        extends EntryBuilder<RecipeType<T>, RecipeTypeEntry<T>, P, RecipeTypeBuilder<T, S, P>> {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final SmartRecipeSerializer.Factory<T, S> serializer;
    @Nullable
    protected Supplier<RecipeType<?>> existingType = null;

    public RecipeTypeBuilder(Registrate registrate, String id, P parent,
                             SmartRecipeSerializer.Factory<T, S> serializer) {
        super(registrate, id, parent);
        this.serializer = serializer;
    }

    public RecipeTypeBuilder<T, S, P> existingType(Supplier<RecipeType<?>> existingType) {
        this.existingType = existingType;
        return self();
    }

    public void registerSerializer(IForgeRegistry<RecipeSerializer<?>> registry) {
        LOGGER.debug("register object {} {}:{}", registry.getRegistryName(), this.registrate.modid, this.id);
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

    public Supplier<RecipeType<?>> getExistingType() {
        assert this.existingType != null;
        return this.existingType;
    }

    @Override
    protected RecipeTypeEntry<T> createEntry() {
        return this.registrate.recipeTypeHandler.register(this);
    }
}
