package org.shsts.tinactory.registrate.handler;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.shsts.tinactory.core.SmartBlockEntityType;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CapabilityHandler {
    private final Registrate registrate;
    private final List<Class<?>> capabilities = new ArrayList<>();

    public CapabilityHandler(Registrate registrate) {
        this.registrate = registrate;
    }

    public void onRegisterEvent(RegisterCapabilitiesEvent event) {
        for (var cap : this.capabilities) {
            event.register(cap);
        }
        this.capabilities.clear();
    }

    public <T> RegistryEntry<Capability<T>> register(Class<T> clazz) {
        this.capabilities.add(clazz);
        // ignore id
        return new RegistryEntry<>(this.registrate.modid, "",
                () -> CapabilityManager.get(new CapabilityToken<>() {}));
    }

    public void onAttachBlockEntity(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject().getType() instanceof SmartBlockEntityType<?> type) {
            type.attachCapabilities(event);
        }
    }
}
