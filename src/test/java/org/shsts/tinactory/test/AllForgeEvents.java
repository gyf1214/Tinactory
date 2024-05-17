package org.shsts.tinactory.test;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AllForgeEvents {
    @SubscribeEvent
    public static void onRegisterCommand(RegisterCommandsEvent event) {
        AllCommands.register(event.getDispatcher());
    }
}
