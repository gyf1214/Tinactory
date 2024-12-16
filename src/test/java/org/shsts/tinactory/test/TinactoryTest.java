package org.shsts.tinactory.test;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.shsts.tinactory.datagen.content.AllData;
import org.shsts.tinycorelib.datagen.api.IDataGen;
import org.shsts.tinycorelib.datagen.api.ITinyDataGen;
import org.slf4j.Logger;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod(TinactoryTest.ID)
public class TinactoryTest {
    public static final String ID = "tinactory_test";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ITinyDataGen DATA_CORE;
    public static IDataGen DATA_GEN;

    public TinactoryTest() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::onGatherData);
        modEventBus.addListener(TinactoryTest::init);
    }

    private static void init(FMLCommonSetupEvent event) {
        LOGGER.info("hello TinactoryTest!");
    }

    private void onGatherData(GatherDataEvent event) {
        DATA_CORE = ITinyDataGen.get();
        DATA_GEN = DATA_CORE.dataGen(REGISTRATE);

        AllData.init();
        DATA_GEN.onGatherData(event);
    }
}
