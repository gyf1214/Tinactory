package org.shsts.tinactory.registrate.handler;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import org.shsts.tinactory.registrate.CapabilityEntry;
import org.shsts.tinactory.registrate.Registrate;

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

    public <T> CapabilityEntry<T> register(Class<T> clazz, CapabilityToken<T> token) {
        this.capabilities.add(clazz);
        return new CapabilityEntry<>(this.registrate.modid, token);
    }
}
