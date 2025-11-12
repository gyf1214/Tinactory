package org.shsts.tinactory.datagen.content.language;

import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.shsts.tinactory.core.common.MetaConsumer;
import org.shsts.tinactory.core.util.LocHelper;
import org.shsts.tinactory.datagen.provider.LanguageDataProvider;

import java.util.function.BiConsumer;

import static org.shsts.tinactory.datagen.content.AllData.initDelayed;
import static org.shsts.tinactory.test.TinactoryTest.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LanguageMeta extends MetaConsumer {
    public LanguageMeta() {
        super("Language");
    }

    private void parse(JsonObject jo, String member, BiConsumer<String, String> cons) {
        var jo1 = GsonHelper.getAsJsonObject(jo, member);
        for (var entry : jo1.entrySet()) {
            cons.accept(entry.getKey(), GsonHelper.convertToString(entry.getValue(), member));
        }
    }

    @Override
    protected void doAcceptMeta(ResourceLocation loc, JsonObject jo) {
        var locale = LocHelper.name(loc.getPath(), -1);
        var splitter = GsonHelper.getAsString(jo, "splitter", " ");
        var processor = new LanguageProcessor(locale, splitter);

        parse(jo, "words", processor::word);
        parse(jo, "patterns", processor::pattern);
        parse(jo, "extras", processor::extra);

        initDelayed(() -> DATA_GEN.addProvider((dataGen, event) ->
            new LanguageDataProvider(dataGen, event, locale, processor)));
    }
}
