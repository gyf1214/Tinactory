package org.shsts.tinactory.unit.autocraft;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.autocraft.AutocraftComponent;
import org.shsts.tinactory.core.autocraft.api.IAutocraftService;
import org.shsts.tinactory.core.autocraft.api.ICpuRuntime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AutocraftRuntimeApiTest {
    @Test
    void cpuRuntimeShouldRegisterWithoutSubnet() {
        assertDoesNotThrow(() -> ICpuRuntime.class.getMethod("registerCpu", IMachine.class, IAutocraftService.class));
        assertThrows(
            NoSuchMethodException.class,
            () -> ICpuRuntime.class.getMethod("registerCpu", IMachine.class, BlockPos.class, IAutocraftService.class));
    }

    @Test
    void autocraftComponentShouldRegisterWithoutSubnet() {
        assertDoesNotThrow(
            () -> AutocraftComponent.class.getMethod("registerCpu", IMachine.class, IAutocraftService.class));
        assertThrows(
            NoSuchMethodException.class,
            () -> AutocraftComponent.class.getMethod(
                "registerCpu",
                IMachine.class,
                BlockPos.class,
                IAutocraftService.class));
    }
}
