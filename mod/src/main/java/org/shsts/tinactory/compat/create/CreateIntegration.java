package org.shsts.tinactory.compat.create;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.machine.MachineSet;
import org.shsts.tinactory.core.gui.Layout;

import java.util.Map;

import static org.shsts.tinactory.Tinactory.CORE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class CreateIntegration {
    public static MachineSet MOTORS = new MachineSet(Layout.EMPTY_SET, Map.of());
    public static MachineSet GENERATORS = new MachineSet(Layout.EMPTY_SET, Map.of());

    private CreateIntegration() {}

    public static void init() {
        CORE.registerMeta("create", new CreateMeta()).execute();
    }
}
