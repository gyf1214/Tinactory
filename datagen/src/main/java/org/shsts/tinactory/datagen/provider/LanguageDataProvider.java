package org.shsts.tinactory.datagen.provider;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.shsts.tinactory.datagen.content.language.LanguageProcessor;
import org.shsts.tinycorelib.datagen.api.IDataGen;

import static org.shsts.tinactory.datagen.TinactoryDatagen.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LanguageDataProvider extends LanguageProvider {
    private final String modid;
    private final String locale;
    private final LanguageProcessor processor;

    public LanguageDataProvider(IDataGen dataGen, GatherDataEvent event,
        String locale, LanguageProcessor processor) {
        super(event.getGenerator().getPackOutput(), dataGen.modid(), locale);
        this.modid = dataGen.modid();
        this.locale = locale;
        this.processor = processor;
        DATA_GEN.trackLocale(locale);
    }

    @Override
    protected void addTranslations() {
        var trackedKeys = DATA_GEN.getTrackedLang();
        processor.process(trackedKeys, this, $ -> DATA_GEN.processLang(locale, $));
    }

    @Override
    public String getName() {
        return super.getName() + " from " + modid;
    }
}
