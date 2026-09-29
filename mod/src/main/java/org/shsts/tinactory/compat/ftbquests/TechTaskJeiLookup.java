package org.shsts.tinactory.compat.ftbquests;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class TechTaskJeiLookup {
    private static final Consumer<ResourceLocation> NO_OP = unused -> {};
    private static Consumer<ResourceLocation> callback = NO_OP;

    private TechTaskJeiLookup() {}

    public static void install(Consumer<ResourceLocation> lookup) {
        callback = lookup;
    }

    public static void clear() {
        callback = NO_OP;
    }

    public static void open(ResourceLocation technologyId) {
        callback.accept(technologyId);
    }
}
