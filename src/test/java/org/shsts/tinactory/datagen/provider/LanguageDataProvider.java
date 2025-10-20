package org.shsts.tinactory.datagen.provider;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.HashCache;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.shsts.tinactory.datagen.content.LanguageProcessor;
import org.shsts.tinycorelib.datagen.api.IDataGen;

import java.io.IOException;

import static org.shsts.tinactory.test.TinactoryTest.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LanguageDataProvider extends LanguageProvider {
    private final String modid;
    private final LanguageProcessor processor;

    public LanguageDataProvider(IDataGen dataGen, GatherDataEvent event,
        String locale, LanguageProcessor processor) {
        super(event.getGenerator(), dataGen.modid(), locale);
        this.modid = dataGen.modid();
        this.processor = processor;
    }

    @Override
    protected void addTranslations() {}

    @Override
    public void run(HashCache cache) throws IOException {
        var trackedKeys = DATA_GEN.getTrackedLang();
        processor.process(trackedKeys, this, DATA_GEN::processLang);
        super.run(cache);
    }

    @Override
    public String getName() {
        return super.getName() + " from " + modid;
    }
}
