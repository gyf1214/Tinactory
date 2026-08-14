package org.shsts.tinactory;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.shsts.tinactory.api.tech.ITeamProvider;
import org.shsts.tinactory.compat.ftbquests.FtbTeamsTeamProvider;
import org.shsts.tinactory.integration.multiblock.WorldMultiblockManagers;
import org.shsts.tinactory.integration.network.WorldNetworkManagers;
import org.shsts.tinactory.integration.tech.SinglePlayerTeamProvider;
import org.shsts.tinactory.integration.tech.TechManagers;

import static org.shsts.tinactory.AllWorldGens.PLAYER_START_FEATURE;
import static org.shsts.tinactory.TinactoryConfig.CONFIG;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllForgeEvents {
    private static ITeamProvider createFtbTeamsProvider() {
        if (!ModList.get().isLoaded("ftbteams")) {
            throw new IllegalStateException("FTB Teams provider requires the ftbteams mod");
        }
        return new FtbTeamsTeamProvider(TechManagers.server(),
            CONFIG.ftbUnaffiliatedPlayerPolicy.get());
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        var provider = switch (CONFIG.teamProvider.get()) {
            case SINGLE_PLAYER -> new SinglePlayerTeamProvider();
            case FTB_TEAMS -> createFtbTeamsProvider();
        };
        TechManagers.installTeamProvider(provider);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        TechManagers.uninstallTeamProvider();
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        TechManagers.server().addReloadListener(event);
    }

    @SubscribeEvent
    public static void onCreateWorldSpawn(LevelEvent.CreateSpawnPosition event) {
        if (event.getLevel() instanceof ServerLevel world) {
            var spawn = new BlockPos(0, 64, 0);
            if (!world.getBiome(spawn).is(AllWorldGens.VOID_WITH_START_BIOME)) {
                return;
            }
            var pos = spawn.offset(-1, 0, -1);
            PLAYER_START_FEATURE.get().place(FeatureConfiguration.NONE, world,
                world.getChunkSource().getGenerator(), world.random, pos);
            event.getSettings().setSpawn(spawn, 0f);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
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
