package org.shsts.tinactory;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.shsts.tinactory.content.AllBlockEntities;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllClientEvents;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinactory.content.AllEvents1;
import org.shsts.tinactory.content.AllForgeEvents;
import org.shsts.tinactory.content.AllItems;
import org.shsts.tinactory.content.AllMaterials;
import org.shsts.tinactory.content.AllMenus;
import org.shsts.tinactory.content.AllMultiBlocks;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.AllWorldGens;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.registrate.AllRegistries;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinycorelib.api.ITinyCoreLib;
import org.shsts.tinycorelib.api.network.IChannel;
import org.shsts.tinycorelib.api.registrate.IRegistrate;
import org.slf4j.Logger;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@Mod(Tinactory.ID)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Tinactory {
    public static final String ID = "tinactory";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Registrate _REGISTRATE = new Registrate(ID);

    public static ITinyCoreLib CORE;
    public static IRegistrate REGISTRATE;
    public static IChannel CHANNEL;

    private final IEventBus modEventBus;

    public Tinactory() {
        this.modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::onConstructEvent);
    }

    private void onConstructEvent(FMLConstructModEvent event) {
        event.enqueueWork(this::onConstruct);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> event.enqueueWork(this::onConstructClient));
    }

    public void onConstruct() {
        try {
            CORE = ITinyCoreLib.get();
            CHANNEL = CORE.createChannel(modLoc("channel"), "1");
            REGISTRATE = CORE.registrate(ID).setDefaultChannel(CHANNEL);

            ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TinactoryConfig.CONFIG_SPEC);

            AllRegistries.init();

            AllRecipes.init();
            AllCapabilities.init();
            AllEvents1.init();
            AllEvents.init();
            AllNetworks.init();

            AllMaterials.init();
            AllMenus.init();
            AllItems.init();
            AllBlockEntities.init();
            AllMultiBlocks.init();

            TechManager.init();
            AllWorldGens.init();

            REGISTRATE.register(modEventBus);
            _REGISTRATE.register(modEventBus);
            modEventBus.addListener(Tinactory::init);
            MinecraftForge.EVENT_BUS.register(AllForgeEvents.class);
        } catch (Throwable e) {
            LOGGER.error("Fatal error encountered during construct!", e);
        }
    }

    public void onConstructClient() {
        TechManager.initClient();

        REGISTRATE.registerClient(modEventBus);
        _REGISTRATE.registerClient(modEventBus);
        modEventBus.addListener(Tinactory::initClient);
        MinecraftForge.EVENT_BUS.register(AllClientEvents.class);
    }

    private static void init(FMLCommonSetupEvent event) {
        LOGGER.info("hello Tinactory!");
    }

    private static void initClient(FMLClientSetupEvent event) {
        LOGGER.info("hello Tinactory client!");
    }
}
