package org.shsts.tinactory.datagen.provider;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.shsts.tinactory.core.tech.Technology;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.datagen.builder.TechBuilder;
import org.shsts.tinycorelib.datagen.api.IDataGen;
import org.shsts.tinycorelib.datagen.api.IDataHandler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechProvider implements DataProvider {
    public static final ExistingFileHelper.ResourceType RESOURCE_TYPE =
        new ExistingFileHelper.ResourceType(PackType.SERVER_DATA, ".json", "technologies");

    private final String modid;
    private final IDataHandler<TechProvider> handler;
    private final PackOutput.PathProvider pathProvider;
    private final ExistingFileHelper existingFileHelper;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;
    private final List<TechBuilder<?>> techs = new ArrayList<>();

    public TechProvider(IDataGen dataGen,
        IDataHandler<TechProvider> handler, GatherDataEvent event) {
        this.modid = dataGen.modid();
        this.handler = handler;
        this.pathProvider = event.getGenerator().getPackOutput()
            .createPathProvider(PackOutput.Target.DATA_PACK, "technologies");
        this.existingFileHelper = event.getExistingFileHelper();
        this.lookupProvider = event.getLookupProvider();
    }

    public void addTech(TechBuilder<?> builder) {
        existingFileHelper.trackGenerated(builder.loc(), RESOURCE_TYPE);
        techs.add(builder);
    }

    private Path getPath(ResourceLocation loc) {
        return pathProvider.json(loc);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return lookupProvider.thenCompose(registries -> {
            handler.register(this);
            var futures = techs.stream().map(tech -> {
                tech.validate(existingFileHelper);
                var jo = CodecHelper.encodeJson(registries, Technology.CODEC, tech.buildObject());
                return DataProvider.saveStable(output, jo, getPath(tech.loc()));
            }).toArray(CompletableFuture[]::new);
            return CompletableFuture.allOf(futures);
        });
    }

    @Override
    public String getName() {
        return "Technologies: " + modid;
    }
}
