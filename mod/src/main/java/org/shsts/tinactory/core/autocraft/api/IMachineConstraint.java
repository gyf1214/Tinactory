package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.electric.Voltage;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMachineConstraint {
    String typeId();

    default boolean matches(IMachine machine, Voltage voltage) {
        return true;
    }

    default boolean matchesRoute(PortDirection dir, int index, IStackKey key, long amount, int port,
        PortType portType) {
        return true;
    }

    default Optional<Runnable> configureLease(IMachine machine) {
        return Optional.empty();
    }
}
