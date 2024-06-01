package org.shsts.tinactory.test;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod(TinactoryTest.ID)
public class TinactoryTest {
    public static final String ID = "tinactory_test";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TinactoryTest() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(TinactoryTest::init);
        MinecraftForge.EVENT_BUS.register(AllForgeEvents.class);
    }

    private static void init(FMLCommonSetupEvent event) {
        LOGGER.info("hello TinactoryTest!");
    }
}
