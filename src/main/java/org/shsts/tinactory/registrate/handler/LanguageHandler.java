package org.shsts.tinactory.registrate.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.shsts.tinactory.content.model.LanguageProcessor;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LanguageHandler extends DataHandler<LanguageProvider> {
    public LanguageHandler(Registrate registrate) {
        super(registrate);
    }

    private class Provider extends LanguageProvider {
        private final Gson gson = new Gson();
        private final ExistingFileHelper existingFileHelper;
        private final Set<String> trackedKeys = new HashSet<>();

        public Provider(DataGenerator gen, ExistingFileHelper existingFileHelper) {
            super(gen, registrate.modid, "en_us");
            this.existingFileHelper = existingFileHelper;
        }

        private void track(Item item) {
            trackedKeys.add(item.getDescriptionId());
        }

        private void track(Block block) {
            trackedKeys.add(block.getDescriptionId());
        }

        private JsonObject readJson(ResourceLocation loc) throws IOException {
            assert existingFileHelper.exists(loc, PackType.CLIENT_RESOURCES);
            var resource = existingFileHelper.getResource(loc, PackType.CLIENT_RESOURCES);
            try (var is = resource.getInputStream();
                 var br = new InputStreamReader(is)) {
                return gson.fromJson(br, JsonObject.class);
            }
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
            LanguageHandler.this.register(this);
            processTracked();
            super.run(cache);
        }
    }

    public void track(String key) {
        addCallback(prov -> ((Provider) prov).trackedKeys.add(key));
    }

    public void item(RegistryEntry<? extends Item> entry) {
        addCallback(prov -> ((Provider) prov).track(entry.get()));
    }

    public void block(RegistryEntry<? extends Block> entry) {
        addCallback(prov -> ((Provider) prov).track(entry.get()));
    }

    @Override
    public void onGatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        generator.addProvider(new Provider(generator, event.getExistingFileHelper()));
    }
}
