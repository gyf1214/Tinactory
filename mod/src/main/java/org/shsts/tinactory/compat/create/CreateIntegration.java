package org.shsts.tinactory.compat.create;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import static org.shsts.tinactory.Tinactory.CORE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class CreateIntegration {
    private CreateIntegration() {}

    static {
        CORE.registerMeta("create", new CreateMeta()).execute();
    }

    public static void init() {}
}
