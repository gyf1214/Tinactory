package org.shsts.tinactory.datagen.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.shsts.tinactory.datagen.builder.TechBuilder;
import org.shsts.tinycorelib.datagen.api.IDataGen;
import org.shsts.tinycorelib.datagen.api.IDataHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechProvider implements DataProvider {
    public static final ExistingFileHelper.ResourceType RESOURCE_TYPE =
        new ExistingFileHelper.ResourceType(PackType.SERVER_DATA, ".json", "technologies");

    private final String modid;
    private final IDataHandler<TechProvider> handler;
    private final DataGenerator generator;
    private final ExistingFileHelper existingFileHelper;
    private final Gson gson = (new GsonBuilder()).setPrettyPrinting().create();

    private final List<TechBuilder<?>> techs = new ArrayList<>();

    public TechProvider(IDataGen dataGen,
        IDataHandler<TechProvider> handler, GatherDataEvent event) {
        this.modid = dataGen.modid();
        this.handler = handler;
        this.generator = event.getGenerator();
        this.existingFileHelper = event.getExistingFileHelper();
    }

    public void addTech(TechBuilder<?> builder) {
        existingFileHelper.trackGenerated(builder.loc(), RESOURCE_TYPE);
        techs.add(builder);
    }

    private Path getPath(ResourceLocation loc) {
        return generator.getOutputFolder()
            .resolve("data/" + loc.getNamespace() + "/technologies/" + loc.getPath() + ".json");
    }

    @Override
    public void run(HashCache cache) throws IOException {
        handler.register(this);
        for (var tech : techs) {
            tech.validate(existingFileHelper);
            var jo = tech.buildObject();
            var path = getPath(tech.loc());
            var s = gson.toJson(jo);
            var hash = SHA1.hashUnencodedChars(s).toString();

            if (!Files.exists(path) || !Objects.equals(cache.getHash(path), hash)) {
                Files.createDirectories(path.getParent());
                try (var bw = Files.newBufferedWriter(path)) {
                    bw.write(s);
                }
            }
            cache.putNew(path, hash);
        }
    }

    @Override
    public String getName() {
        return "Technologies: " + modid;
    }
}
