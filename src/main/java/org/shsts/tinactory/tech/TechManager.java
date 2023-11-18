package org.shsts.tinactory.tech;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class TechManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static class ReloadListener implements PreparableReloadListener {
        private static final String PREFIX = "technology";
        private static final String SUFFIX = ".json";

        private final Gson gson = new Gson();

        private static Collection<ResourceLocation> listResources(ResourceManager manager, String prefix) {
            return manager.listResources(prefix, f -> f.endsWith(SUFFIX));
        }

        private <T extends ForgeRegistryEntry<T>> Optional<T>
        loadResource(ResourceManager manager, ResourceLocation loc, Codec<T> codec) {
            var path = loc.getPath();
            var path1 = path.substring(PREFIX.length() + 1, path.length() - SUFFIX.length());
            var loc1 = new ResourceLocation(loc.getNamespace(), path1);
            try (var resource = manager.getResource(loc)) {
                var reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
                var jo = GsonHelper.fromJson(this.gson, reader, JsonObject.class);
                return Optional.of(codec.parse(JsonOps.INSTANCE, jo).getOrThrow(false, $ -> {}).setRegistryName(loc1));
            } catch (IOException | RuntimeException ex) {
                LOGGER.debug("Decode resource {} failed: {}", loc, ex);
            }
            return Optional.empty();
        }

        @Override
        public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager manager,
                                              ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler,
                                              Executor backgroundExecutor, Executor gameExecutor) {

            return stage.wait(Unit.INSTANCE)
                    .thenApplyAsync($ -> listResources(manager, PREFIX).stream()
                            .flatMap(loc -> this.loadResource(manager, loc, Technology.CODEC).stream())
                            .toList(), backgroundExecutor)
                    .thenAcceptAsync(techs -> {
                        TECHNOLOGIES.clear();
                        techs.forEach(tech -> {
                            assert tech.getRegistryName() != null;
                            TECHNOLOGIES.put(tech.getRegistryName(), tech);
                        });
                        techs.forEach(Technology::resolve);
                    }, backgroundExecutor);
        }
    }

    public static final PreparableReloadListener RELOAD_LISTENER = new ReloadListener();

    private static final Map<ResourceLocation, Technology> TECHNOLOGIES = new HashMap<>();

    public static Optional<Technology> techByKey(ResourceLocation loc) {
        return Optional.of(TECHNOLOGIES.get(loc));
    }

    public static void invalidatePlayer(ServerPlayer player) {
        TinactorySavedData.get(player.getLevel()).invalidatePlayer(player.getGameProfile().getId());
    }
}
