package org.shsts.tinactory;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.shsts.tinactory.content.AllBlockEntities;
import org.shsts.tinactory.content.AllBlocks;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllItems;
import org.shsts.tinactory.content.AllWorldGens;
import org.shsts.tinactory.content.network.AllNetworkRegistries;
import org.shsts.tinactory.model.ModelGen;
import org.shsts.tinactory.registrate.AllRegistries;
import org.shsts.tinactory.registrate.Registrate;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod(Tinactory.ID)
public class Tinactory {
    public static final String ID = "tinactory";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Registrate REGISTRATE = new Registrate(ID);

    public Tinactory() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        onCreate(modEventBus);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> onCreateClient(modEventBus));
    }

    public static ResourceLocation modLoc(String id) {
        return new ResourceLocation(ID, id);
    }

    private static void onCreate(IEventBus modEventBus) {
        AllRegistries.init();

        ModelGen.init();
        AllBlocks.init();
        AllItems.init();
        AllCapabilities.init();
        AllBlockEntities.init();

        AllNetworkRegistries.init();
        AllWorldGens.init();

        REGISTRATE.register(modEventBus);
        modEventBus.addListener(Tinactory::init);
    }

    private static void onCreateClient(IEventBus modEventBus) {
        REGISTRATE.registerClient(modEventBus);
        modEventBus.addListener(Tinactory::initClient);
        MinecraftForge.EVENT_BUS.register(AllClientEvents.class);
    }

    private static void init(final FMLCommonSetupEvent event) {
        LOGGER.info("hello Tinactory!");
    }

    private static void initClient(FMLClientSetupEvent event) {
        LOGGER.info("hello Tinactory client!");
    }
}
