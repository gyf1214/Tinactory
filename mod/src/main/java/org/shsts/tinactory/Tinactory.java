package org.shsts.tinactory;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.compat.ftbquests.TechQuestIntegration;
import org.shsts.tinactory.integration.tech.TechManagers;
import org.shsts.tinycorelib.api.ITinyCoreLib;
import org.shsts.tinycorelib.api.registrate.IRegistrate;
import org.slf4j.Logger;

@Mod(TinactoryKeys.ID)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Tinactory {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ITinyCoreLib CORE;
    public static IRegistrate REGISTRATE;

    private final IEventBus modEventBus;
    @Nullable
    private TechQuestIntegration techQuestIntegration = null;

    public Tinactory(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, TinactoryConfig.CONFIG_SPEC);
        this.modEventBus = modEventBus;
        modEventBus.addListener(this::onConstructEvent);
    }

    private void onConstructEvent(FMLConstructModEvent event) {
        onConstruct();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            onConstructClient();
        }
    }

    public void onConstruct() {
        try {
            CORE = ITinyCoreLib.get();
            REGISTRATE = CORE.registrate(TinactoryKeys.ID);

            AllRegistries.init();
            AllMeta.init();

            AllRecipes.init();
            AllCapabilities.init();
            AllEvents.init();
            AllNetworks.init();

            AllMaterials.init();
            AllMenus.init();
            AllItems.init();
            AllBlockEntities.init();
            AllMultiblocks.init();

            TechManagers.init();
            if (ModList.get().isLoaded("ftbquests")) {
                techQuestIntegration = new TechQuestIntegration();
            }
            AllWorldGens.init();

            REGISTRATE.register(modEventBus);
            modEventBus.addListener(Tinactory::init);
            NeoForge.EVENT_BUS.register(AllForgeEvents.class);
        } catch (Throwable e) {
            LOGGER.error("Fatal error encountered during construct!", e);
        }
    }

    public void onConstructClient() {
        TechManagers.initClient();

        REGISTRATE.registerClient(modEventBus);
        modEventBus.addListener(Tinactory::initClient);
        modEventBus.addListener(AllClientEvents::initKeys);
        NeoForge.EVENT_BUS.register(AllClientEvents.class);
    }

    private static void init(FMLCommonSetupEvent event) {
        LOGGER.info("hello Tinactory!");
    }

    private static void initClient(FMLClientSetupEvent event) {
        LOGGER.info("hello Tinactory client!");
    }
}
