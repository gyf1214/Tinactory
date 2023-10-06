package org.shsts.tinactory.registrate;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;
import org.shsts.tinactory.Tinactory;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RegistryEntry<U> implements Supplier<U> {
    public final String modid;
    public final String id;

    @Nullable
    private Supplier<U> supplier;
    @Nullable
    private U object = null;

    public RegistryEntry(String modid, String id) {
        this.modid = modid;
        this.id = id;
        this.supplier = null;
    }

    public RegistryEntry(ResourceLocation loc, RegistryObject<U> registryObject) {
        this.modid = loc.getNamespace();
        this.id = loc.getPath();
        this.supplier = registryObject;
    }

    public static <T, U extends T> RegistryEntry<U>
    create(ResourceLocation loc, ResourceKey<? extends Registry<T>> registry) {
        var object = RegistryObject.<T, U>create(loc, registry, Tinactory.ID);
        return new RegistryEntry<>(loc, object);
    }

    public static <T, U extends T> RegistryEntry<U>
    create(String loc, ResourceKey<? extends Registry<T>> registry) {
        return create(new ResourceLocation(loc), registry);
    }

    public void setObject(U object) {
        this.object = object;
        this.supplier = null;
    }

    @Override
    public U get() {
        assert this.object != null || this.supplier != null;
        if (this.object == null) {
            this.setObject(this.supplier.get());
        }
        return this.object;
    }
}
