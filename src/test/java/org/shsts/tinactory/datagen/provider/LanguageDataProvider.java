package org.shsts.tinactory.datagen.provider;

import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.datagen.content.LanguageProcessor;
import org.shsts.tinycorelib.datagen.api.IDataGen;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.test.TinactoryTest.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LanguageDataProvider extends LanguageProvider {
    private final String modid;
    private final ExistingFileHelper existingFileHelper;

    public LanguageDataProvider(IDataGen dataGen, GatherDataEvent event) {
        super(event.getGenerator(), dataGen.modid(), "en_us");
        this.modid = dataGen.modid();
        this.existingFileHelper = event.getExistingFileHelper();
    }

    private JsonObject readJson(ResourceLocation loc) throws IOException {
        assert existingFileHelper.exists(loc, PackType.CLIENT_RESOURCES);
        var resource = existingFileHelper.getResource(loc, PackType.CLIENT_RESOURCES);
        try (var is = resource.getInputStream();
            var br = new InputStreamReader(is)) {
            return CodecHelper.jsonFromReader(br);
        }
    }

    @Override
    protected void addTranslations() {}

    @Override
    public void run(HashCache cache) throws IOException {
        var trackedKeys = DATA_GEN.getTrackedLang();
        var processor = new LanguageProcessor();
        var extra = readJson(modLoc("lang/extra.json"));
        processor.process(trackedKeys, extra, this, DATA_GEN::processLang);
        super.run(cache);
    }

    @Override
    public String getName() {
        return "Languages: " + modid;
    }
}
