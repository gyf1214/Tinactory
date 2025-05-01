package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.shsts.tinactory.core.common.CapabilityItem;
import org.shsts.tinactory.core.multiblock.MultiblockManager;
import org.shsts.tinactory.core.network.NetworkManager;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.tech.TinactorySavedData;

import java.util.Random;

import static org.shsts.tinactory.content.AllWorldGens.PLAYER_START_FEATURE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllForgeEvents {
    @SubscribeEvent
    public static void onAttachItemStack(AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().getItem() instanceof CapabilityItem capabilityItem) {
            capabilityItem.attachCapabilities(event);
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
            PLAYER_START_FEATURE.get().place(FeatureConfiguration.NONE, world,
                world.getChunkSource().getGenerator(), new Random(), spawn);
            event.getSettings().setSpawn(spawn, 0f);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer serverPlayer) {
            TechManager.server().onPlayerJoin(serverPlayer);
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
        if (!world.isClientSide) {
            NetworkManager.onUnload(world);
            MultiblockManager.onUnload(world);
        }
        if (world.dimension() == Level.OVERWORLD) {
            if (!world.isClientSide) {
                TinactorySavedData.unload();
                TechManager.server().unload();
            } else {
                TechManager.client().unload();
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterCommand(RegisterCommandsEvent event) {
        AllCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onBlockChanged(BlockEvent.NeighborNotifyEvent event) {
        var world = (Level) event.getWorld();
        if (world.isClientSide) {
            return;
        }
        MultiblockManager.get(world).invalidate(event.getPos());
    }
}
