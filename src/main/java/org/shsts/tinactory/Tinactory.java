package org.shsts.tinactory;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.shsts.tinactory.content.AllBlockEntities;
import org.shsts.tinactory.content.AllBlocks;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllItems;
import org.shsts.tinactory.content.AllMaterials;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.AllWorldGens;
import org.shsts.tinactory.content.network.AllNetworks;
import org.shsts.tinactory.core.IPacket;
import org.shsts.tinactory.gui.sync.ContainerSyncHandler;
import org.shsts.tinactory.model.ModelGen;
import org.shsts.tinactory.registrate.AllRegistries;
import org.shsts.tinactory.registrate.Registrate;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiConsumer;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod(Tinactory.ID)
public class Tinactory {
    public static final String ID = "tinactory";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Registrate REGISTRATE = new Registrate(ID);

    private static final String CHANNEL_VERSION = "1";
    private static int msgId = 0;
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ID, "channel"),
            () -> CHANNEL_VERSION,
            CHANNEL_VERSION::equals,
            CHANNEL_VERSION::equals);

    public static <T extends IPacket>
    void registryPacket(Class<T> clazz, Function<FriendlyByteBuf, T> factory,
                        BiConsumer<T, NetworkEvent.Context> handler) {
        CHANNEL.registerMessage(msgId++, clazz, IPacket::serializeToBuf, factory, (msg, ctxSupp) -> {
            var ctx = ctxSupp.get();
            ctx.enqueueWork(() -> handler.accept(msg, ctx));
            ctx.setPacketHandled(true);
        });
    }

    public static <T extends IPacket>
    void registryClientPacket(Class<T> clazz, Function<FriendlyByteBuf, T> factory,
                              BiConsumer<T, NetworkEvent.Context> handler) {
        registryPacket(clazz, factory, (msg, ctx) ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handler.accept(msg, ctx)));
    }

    public Tinactory() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        onCreate(modEventBus);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> onCreateClient(modEventBus));
    }

    private static void onCreate(IEventBus modEventBus) {
        AllRegistries.init();

        ModelGen.init();
        AllRecipes.init();
        AllMaterials.init();
        AllBlocks.init();
        AllItems.init();
        AllCapabilities.init();
        AllBlockEntities.init();
        AllNetworks.init();
        AllWorldGens.init();

        AllRecipes.initRecipes();

        REGISTRATE.register(modEventBus);
        modEventBus.addListener(Tinactory::init);
        MinecraftForge.EVENT_BUS.register(AllEvents.class);
    }

    private static void onCreateClient(IEventBus modEventBus) {
        ContainerSyncHandler.registerPackets();

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
