package org.shsts.tinactory;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.shsts.tinactory.integration.common.CapabilityItem;
import org.shsts.tinactory.integration.common.ItemCapabilityProvider;
import org.shsts.tinactory.integration.multiblock.WorldMultiblockManagers;
import org.shsts.tinactory.integration.network.WorldNetworkManagers;
import org.shsts.tinactory.integration.tech.TechManagers;

import java.util.Random;

import static org.shsts.tinactory.AllWorldGens.PLAYER_START_FEATURE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllForgeEvents {
    @SubscribeEvent
    public static void onAttachItemStack(AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().getItem() instanceof CapabilityItem capabilityItem) {
            capabilityItem.attachCapabilities(event);
        }
        for (var cap : event.getCapabilities().values()) {
            if (cap instanceof ItemCapabilityProvider itemCap) {
                itemCap.init();
            }
        }
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        TechManagers.server().addReloadListener(event);
    }

    @SubscribeEvent
    public static void onCreateWorldSpawn(LevelEvent.CreateSpawnPosition event) {
        if (event.getLevel() instanceof ServerLevel world) {
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
            TechManagers.server().onPlayerJoin(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onLoadWorld(LevelEvent.Load event) {
        var world = (Level) event.getLevel();
        if (!world.isClientSide && world.dimension() == Level.OVERWORLD) {
            TechManagers.loadSavedData((ServerLevel) world);
        }
    }

    @SubscribeEvent
    public static void onUnloadWorld(LevelEvent.Unload event) {
        var world = (Level) event.getLevel();
        if (!world.isClientSide) {
            WorldNetworkManagers.onUnload(world);
            WorldMultiblockManagers.onUnload(world);
        }
        if (world.dimension() == Level.OVERWORLD) {
            if (!world.isClientSide) {
                TechManagers.unloadSavedData();
                TechManagers.server().unload();
            } else {
                TechManagers.client().unload();
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterCommand(RegisterCommandsEvent event) {
        AllCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onBlockChanged(BlockEvent.NeighborNotifyEvent event) {
        var world = (Level) event.getLevel();
        if (world.isClientSide) {
            return;
        }
        WorldMultiblockManagers.get(world).invalidate(event.getPos());
    }
}
