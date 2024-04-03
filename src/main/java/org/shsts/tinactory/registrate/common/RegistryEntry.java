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
        this.loc = new ResourceLocation(this.modid, this.id);
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

    public void setObject(U object) {
        this.object = object;
        this.supplier = null;
    }

    public void setSupplier(Supplier<U> supplier) {
        this.supplier = supplier;
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