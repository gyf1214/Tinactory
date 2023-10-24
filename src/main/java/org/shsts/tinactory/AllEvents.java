package org.shsts.tinactory;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.shsts.tinactory.core.SmartBlockEntityType;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class AllEvents {
    @SubscribeEvent
    public static void onAttachBlockEntity(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject().getType() instanceof SmartBlockEntityType<?> type) {
            type.attachCapabilities(event);
        }
    }
}
