package org.shsts.tinactory.datagen;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.shsts.tinactory.datagen.content.AllData;
import org.shsts.tinactory.datagen.content.AllDataMeta;
import org.shsts.tinycorelib.api.ITinyCoreLib;
import org.shsts.tinycorelib.datagen.api.IDataGen;
import org.shsts.tinycorelib.datagen.api.ITinyDataGen;
import org.slf4j.Logger;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod(TinactoryDatagen.ID)
public class TinactoryDatagen {
    public static final String ID = "tinactory_datagen";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ITinyCoreLib CORE;
    public static ITinyDataGen DATA_CORE;
    public static IDataGen DATA_GEN;

    public TinactoryDatagen() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::onConstruct);
        modEventBus.addListener(this::init);
        modEventBus.addListener(this::onGatherData);
    }

    private void onConstruct(FMLConstructModEvent event) {
        CORE = ITinyCoreLib.get();
        AllDataMeta.init();
    }

    private void init(FMLCommonSetupEvent event) {
        LOGGER.info("hello TinactoryDatagen!");
    }

    private void onGatherData(GatherDataEvent event) {
        DATA_CORE = ITinyDataGen.get();
        DATA_GEN = DATA_CORE.dataGen(REGISTRATE);

        AllData.init();
        DATA_GEN.onGatherData(event);
    }
}
