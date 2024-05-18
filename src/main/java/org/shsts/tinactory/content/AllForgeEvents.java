package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.shsts.tinactory.core.common.SmartBlockEntityType;
import org.shsts.tinactory.core.network.NetworkManager;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.tech.TinactorySavedData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllForgeEvents {
    @SubscribeEvent
    public static void onAttachBlockEntity(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject().getType() instanceof SmartBlockEntityType<?> type) {
            type.attachCapabilities(event);
        }
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        TechManager.server().addReloadListener(event);
    }

    @SubscribeEvent
    public static void onCreateWorldSpawn(WorldEvent.CreateSpawnPosition event) {
        if (event.getWorld() instanceof ServerLevel world) {
            var spawn = new BlockPos(0, 64, 0);
            if (!world.getBiome(spawn).is(AllWorldGens.VOID_BIOME)) {
                return;
            }
            AllWorldGens.PLAYER_START_FEATURE.get().place(FeatureConfiguration.NONE, world,
                    world.getChunkSource().getGenerator(), new Random(), spawn);
            event.getSettings().setSpawn(spawn, 0.0f);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLoadWorld(WorldEvent.Load event) {
        var world = (Level) event.getWorld();
        if (!world.isClientSide && world.dimension() == Level.OVERWORLD) {
            TinactorySavedData.load((ServerLevel) world);
        }
    }

    @SubscribeEvent
    public static void onUnloadWorld(WorldEvent.Unload event) {
        var world = (Level) event.getWorld();
        NetworkManager.onUnload(world);
        if (!world.isClientSide && world.dimension() == Level.OVERWORLD) {
            TinactorySavedData.unload();
            TechManager.server().unload();
        }
    }

    @SubscribeEvent
    public static void onRegisterCommand(RegisterCommandsEvent event) {
        AllCommands.register(event.getDispatcher());
    }
}
