package org.shsts.tinactory.gametest.create;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import org.shsts.tinactory.api.TinactoryKeys;

@EventBusSubscriber(modid = TinactoryKeys.ID, bus = EventBusSubscriber.Bus.MOD)
public final class CreateGameTestRegistration {
    private CreateGameTestRegistration() {}

    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        if (ModList.get().isLoaded("create")) {
            event.register(CreateIntegrationGameTest.class);
        }
    }
}
