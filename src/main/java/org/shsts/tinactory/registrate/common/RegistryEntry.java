package org.shsts.tinactory.registrate.common;

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
    public final ResourceLocation loc;

    @Nullable
    private Supplier<U> supplier;
    @Nullable
    private U object = null;

    public RegistryEntry(String modid, String id, @Nullable Supplier<U> supplier) {
        this.modid = modid;
        this.id = id;
        this.loc = new ResourceLocation(modid, id);
        this.supplier = supplier;
    }

    public RegistryEntry(String modid, String id) {
        this(modid, id, null);
    }

    public RegistryEntry(ResourceLocation loc, Supplier<U> supplier) {
        this.modid = loc.getNamespace();
        this.id = loc.getPath();
        this.loc = loc;
        this.supplier = supplier;
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

    public void setObject(U value) {
        object = value;
        supplier = null;
    }

    public void setSupplier(Supplier<U> value) {
        supplier = value;
    }

    @Override
    public U get() {
        assert object != null || supplier != null;
        if (object == null) {
            setObject(supplier.get());
        }
        return object;
    }
}
