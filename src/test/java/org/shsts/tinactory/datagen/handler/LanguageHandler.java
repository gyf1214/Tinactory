package org.shsts.tinactory.datagen.handler;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.datagen.DataGen;
import org.shsts.tinactory.datagen.content.LanguageProcessor;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LanguageHandler extends DataHandler<LanguageProvider> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private class Provider extends LanguageProvider {
        private final Gson gson = new Gson();
        private final ExistingFileHelper existingFileHelper;
        private final Set<String> trackedKeys = new HashSet<>();

        public Provider(DataGenerator gen, ExistingFileHelper existingFileHelper) {
            super(gen, dataGen.modid, "en_us");
            this.existingFileHelper = existingFileHelper;
        }

        private JsonObject readJson(ResourceLocation loc) throws IOException {
            assert existingFileHelper.exists(loc, PackType.CLIENT_RESOURCES);
            var resource = existingFileHelper.getResource(loc, PackType.CLIENT_RESOURCES);
            try (var is = resource.getInputStream();
                 var br = new InputStreamReader(is)) {
                return gson.fromJson(br, JsonObject.class);
            }
        }

        private void gatherTracked() {
            trackedKeys.addAll(Tinactory.REGISTRATE.translationKeys);
            trackedKeys.addAll(DataGen.REGISTRATE.translationKeys);
            LOGGER.debug("{} add {} tracked translations", LanguageHandler.this, trackedKeys.size());
        }

        private void processTracked() throws IOException {
            var processor = new LanguageProcessor();
            var extra = readJson(modLoc("lang/extra.json"));
            processor.process(trackedKeys, extra, this);
            trackedKeys.clear();
        }

        @Override
        protected void addTranslations() {}

        @Override
        public void run(HashCache cache) throws IOException {
            gatherTracked();
            processTracked();
            super.run(cache);
        }
    }

    public LanguageHandler(DataGen dataGen) {
        super(dataGen);
    }

    @Override
    protected LanguageProvider createProvider(GatherDataEvent event) {
        return new Provider(event.getGenerator(), event.getExistingFileHelper());
    }
}
