package org.shsts.tinactory;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.shsts.tinactory.content.AllBlockEntities;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllClientEvents;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinactory.content.AllForgeEvents;
import org.shsts.tinactory.content.AllItems;
import org.shsts.tinactory.content.AllMaterials;
import org.shsts.tinactory.content.AllMenus;
import org.shsts.tinactory.content.AllMultiBlocks;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.AllWorldGens;
import org.shsts.tinactory.core.common.IPacket;
import org.shsts.tinactory.core.gui.sync.MenuEventHandler;
import org.shsts.tinactory.core.gui.sync.MenuSyncHandler;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.registrate.AllRegistries;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinycorelib.api.ITinyCoreLib;
import org.shsts.tinycorelib.api.network.IChannel;
import org.shsts.tinycorelib.api.registrate.IRegistrate;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod(Tinactory.ID)
public class Tinactory {
    public static final String ID = "tinactory";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Registrate _REGISTRATE = new Registrate(ID);

    private static final String CHANNEL_VERSION = "1";
    private static final AtomicInteger MSG_ID = new AtomicInteger(0);
    public static final SimpleChannel _CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(ID, "channel1"),
        () -> CHANNEL_VERSION,
        CHANNEL_VERSION::equals,
        CHANNEL_VERSION::equals);

    public static <T extends IPacket> void registryPacket(Class<T> clazz, Supplier<T> constructor,
        BiConsumer<T, NetworkEvent.Context> handler) {
        _CHANNEL.registerMessage(MSG_ID.getAndIncrement(), clazz, IPacket::serializeToBuf,
            (buf) -> {
                var p = constructor.get();
                p.deserializeFromBuf(buf);
                return p;
            }, (msg, ctxSupp) -> {
                var ctx = ctxSupp.get();
                ctx.enqueueWork(() -> handler.accept(msg, ctx));
                ctx.setPacketHandled(true);
            });
    }

    public static <T extends IPacket> void registryClientPacket(Class<T> clazz, Supplier<T> constructor,
        BiConsumer<T, NetworkEvent.Context> handler) {
        registryPacket(clazz, constructor, (msg, ctx) ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handler.accept(msg, ctx)));
    }

    public static <P extends IPacket> void sendToServer(P packet) {
        _CHANNEL.sendToServer(packet);
    }

    public static <P extends IPacket> void sendToPlayer(ServerPlayer player, P packet) {
        _CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

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
            AllEvents.init();
            AllNetworks.init();

            AllMaterials.init();
            AllMenus.init();
            AllItems.init();
            AllBlockEntities.init();
            AllMultiBlocks.init();

            TechManager.init();
            AllWorldGens.init();
            MenuSyncHandler.init();
            MenuEventHandler.init();

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
