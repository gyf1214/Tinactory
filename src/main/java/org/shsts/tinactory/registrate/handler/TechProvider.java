package org.shsts.tinactory.registrate.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class TechProvider implements DataProvider {
    private static final ExistingFileHelper.ResourceType RESOURCE_TYPE =
            new ExistingFileHelper.ResourceType(PackType.SERVER_DATA, ".json", "technologies");

    private final DataGenerator generator;
    private final String modid;
    private final ExistingFileHelper existingFileHelper;
    private final Gson gson = (new GsonBuilder()).setPrettyPrinting().create();

    private record TechInfo(ResourceLocation loc, List<ResourceLocation> depends, long maxProgress) {
        public JsonObject serialize() {
            var jo = new JsonObject();
            jo.addProperty("max_progress", maxProgress);
            var ja = new JsonArray();
            depends.forEach(d -> ja.add(d.toString()));
            jo.add("depends", ja);
            return jo;
        }
    }

    private final List<TechInfo> techs = new ArrayList<>();

    protected TechProvider(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
        this.generator = generator;
        this.modid = modid;
        this.existingFileHelper = existingFileHelper;
    }

    public void addTech(ResourceLocation loc, List<ResourceLocation> depends, long maxProgress) {
        existingFileHelper.trackGenerated(loc, RESOURCE_TYPE);
        techs.add(new TechInfo(loc, depends, maxProgress));
    }

    protected abstract void addTechs();

    private Path getPath(ResourceLocation loc) {
        return generator.getOutputFolder().resolve("data/" + loc.getNamespace() + "/technologies/"
                + loc.getPath() + ".json");
    }

    private void validate(TechInfo tech) {
        for (var loc : tech.depends) {
            if (!existingFileHelper.exists(loc, RESOURCE_TYPE)) {
                throw new IllegalStateException("Technology at %s does not exist".formatted(loc));
            }
        }
    }

    @Override
    public void run(HashCache cache) throws IOException {
        addTechs();
        for (var tech : techs) {
            validate(tech);
            var jo = tech.serialize();
            var path = getPath(tech.loc);
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
        return modid + " technology";
    }
}
