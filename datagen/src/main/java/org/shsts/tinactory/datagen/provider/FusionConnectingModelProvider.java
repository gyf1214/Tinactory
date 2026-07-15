package org.shsts.tinactory.datagen.provider;

import com.google.gson.JsonObject;
import com.supermartijn642.fusion.api.model.DefaultModelTypes;
import com.supermartijn642.fusion.api.model.types.connecting.ConnectingModelData;
import com.supermartijn642.fusion.api.texture.types.connecting.predicates.DefaultConnectionPredicates;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.shsts.tinycorelib.datagen.api.IDataGen;
import org.shsts.tinycorelib.datagen.api.IDataHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class FusionConnectingModelProvider implements DataProvider {
    public static final ExistingFileHelper.ResourceType RESOURCE_TYPE =
        new ExistingFileHelper.ResourceType(PackType.CLIENT_RESOURCES, ".json", "models");

    private final IDataHandler<FusionConnectingModelProvider> handler;
    private final ExistingFileHelper existingFileHelper;
    private final PackOutput.PathProvider pathProvider;
    private final List<Entry> entries = new ArrayList<>();

    public FusionConnectingModelProvider(IDataGen dataGen,
        IDataHandler<FusionConnectingModelProvider> handler, GatherDataEvent event) {
        this.handler = handler;
        existingFileHelper = event.getExistingFileHelper();
        pathProvider = event.getGenerator().getPackOutput()
            .createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
    }

    public void addModel(ResourceLocation id, ResourceLocation parent, ResourceLocation texture,
        ResourceLocation emissive) {
        addModel(id, parent, texture, emissive, null);
    }

    public void addModel(ResourceLocation id, ResourceLocation parent, ResourceLocation texture,
        ResourceLocation emissive, @Nullable ResourceLocation renderType) {
        existingFileHelper.trackGenerated(id, RESOURCE_TYPE);
        entries.add(new Entry(id, parent, texture, emissive, renderType));
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        if (handler != null) {
            handler.register(this);
        }
        var futures = entries.stream().map(entry -> {
            var builder = ConnectingModelData.builder()
                .parent(entry.parent())
                .material("all", entry.texture())
                .material("all_emissive", entry.emissive())
                .defaultConnections(DefaultConnectionPredicates.isSameBlock());
            JsonObject json = DefaultModelTypes.CONNECTING.serialize(builder.build());
            json.addProperty("loader", "fusion:model");
            if (entry.renderType() != null) {
                json.addProperty("render_type", entry.renderType().toString());
            }
            return DataProvider.saveStable(output, json, pathProvider.json(entry.id()));
        }).toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }

    @Override
    public String getName() {
        return "Fusion connecting models";
    }

    private record Entry(ResourceLocation id, ResourceLocation parent, ResourceLocation texture,
        ResourceLocation emissive, @Nullable ResourceLocation renderType) {}
}
