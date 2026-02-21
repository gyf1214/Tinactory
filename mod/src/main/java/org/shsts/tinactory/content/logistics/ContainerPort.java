package org.shsts.tinactory.content.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.ContainerAccess;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.SlotType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ContainerPort(SlotType type, IPort internal, IPort menu, IPort external) {
    public static final ContainerPort EMPTY = new ContainerPort(
        SlotType.NONE, IPort.EMPTY, IPort.EMPTY, IPort.EMPTY);

    public IPort get(ContainerAccess access) {
        return switch (access) {
            case INTERNAL -> internal;
            case MENU -> menu;
            case EXTERNAL -> external;
        };
    }
}
