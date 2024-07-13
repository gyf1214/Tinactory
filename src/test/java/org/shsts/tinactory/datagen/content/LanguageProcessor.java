package org.shsts.tinactory.datagen.content;

import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.common.data.LanguageProvider;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LanguageProcessor {
    private static final String MISSING_TR = "<MISSING TR>";

    private record Processor(Pattern pattern, Function<Matcher, String> func) {}

    private final List<Processor> processors = new ArrayList<>();

    private void pattern(String pattern, Function<Matcher, String> func) {
        processors.add(new Processor(Pattern.compile("^" + pattern + "$"), func));
    }

    private String normalize(Matcher matcher, int group) {
        return Arrays.stream(matcher.group(group).split("_"))
                .map(str -> str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    private String capitalize(Matcher matcher, int group) {
        return matcher.group(group).toUpperCase();
    }

    private String fmt(String a, Object... args) {
        return a.formatted(args);
    }

    public LanguageProcessor() {
        pattern("block[.]tinactory[.]material[.]ore[.](.*)", matcher ->
                fmt("%s Ore", normalize(matcher, 1)));
        pattern("block[.]tinactory[.]network[.](.*)[.](.*)", matcher ->
                fmt("%s %s", capitalize(matcher, 1), normalize(matcher, 2)));
        pattern("item[.]tinactory[.]network[.](.*)[.](.*)", matcher ->
                fmt("%s %s", capitalize(matcher, 1), normalize(matcher, 2)));
        pattern("item[.]tinactory[.]component[.](.*)[.](.*)", matcher ->
                fmt("%s %s", capitalize(matcher, 1), normalize(matcher, 2)));
        pattern("item[.]tinactory[.]material[.]dust_impure[.](.*)", matcher ->
                fmt("Impure Pile of %s", normalize(matcher, 1)));
        pattern("item[.]tinactory[.]material[.]dust_pure[.](.*)", matcher ->
                fmt("Purified Pile of %s", normalize(matcher, 1)));
        pattern("item[.]tinactory[.]material[.]dust_tiny[.](.*)", matcher ->
                fmt("Tiny Pile of %s Dust", normalize(matcher, 1)));
        pattern("item[.]tinactory[.]material[.]crushed_(.*)[.](.*)", matcher ->
                fmt("%s %s Ore", normalize(matcher, 1), normalize(matcher, 2)));
        pattern("item[.]tinactory[.]material[.]crushed[.](.*)", matcher ->
                fmt("Crushed %s Ore", normalize(matcher, 1)));
        pattern("item[.]tinactory[.]material[.]raw[.](.*)", matcher ->
                fmt("Raw %s Ore", normalize(matcher, 1)));
        pattern("item[.]tinactory[.]material[.]magnetic[.](.*)", matcher ->
                fmt("Magnetic %s Stick", normalize(matcher, 1)));
        pattern("item[.]tinactory[.]material[.](.*)[.](.*)", matcher ->
                fmt("%s %s", normalize(matcher, 2), normalize(matcher, 1)));
        pattern("item[.]tinactory[.]tool[.](.*)[.](.*)", matcher ->
                fmt("%s %s", normalize(matcher, 2), normalize(matcher, 1)));
        pattern("block[.]tinactory[.]machine[.](.*)[.](.*)", matcher ->
                fmt("%s %s", capitalize(matcher, 1), normalize(matcher, 2)));
        pattern("block[.]tinactory[.]primitive[.](.*)", matcher ->
                fmt("Primitive %s", normalize(matcher, 1)));
        pattern("block[.]tinactory[.]multi_block[.](.*)[.]interface", matcher ->
                fmt("%s Multiblock Interface", capitalize(matcher, 1)));
        pattern("block[.]tinactory[.]multi_block[.]coil[.](.*)", matcher ->
                fmt("%s Furnace Coil", normalize(matcher, 1)));
        pattern("tinactory[.]technology[.]ore[.](.*)[.]details", matcher ->
                fmt("Allow analyzing Raw %s Ore from base stones.", normalize(matcher, 1)));
        pattern("tinactory[.]technology[.]ore[.](.*)", matcher ->
                fmt("%s Ore", normalize(matcher, 1)));
        pattern("tinactory[.]technology[.]ore_base[.](.*)[.]details", matcher ->
                fmt("Unlock ores from %s.", normalize(matcher, 1)));
        pattern("tinactory[.]technology[.]ore_base[.](.*)", matcher ->
                fmt("%s Ores", normalize(matcher, 1)));
        pattern("tinactory[.]jei[.]category[.](.*)", matcher ->
                normalize(matcher, 1));
    }

    private Optional<String> process(String key) {
        for (var processor : processors) {
            var matcher = processor.pattern.matcher(key);
            if (matcher.find()) {
                return Optional.of(processor.func.apply(matcher));
            }
        }
        return Optional.empty();
    }

    public void process(Set<String> keys, JsonObject extra,
                        LanguageProvider prov, Consumer<String> onProcess) {
        for (var key : keys) {
            if (extra.has(key)) {
                onProcess.accept(key);
                continue;
            }
            var value = process(key);
            if (value.isPresent()) {
                onProcess.accept(key);
            }
            prov.add(key, value.orElse(MISSING_TR));
        }
        for (var entry : extra.entrySet()) {
            prov.add(entry.getKey(), entry.getValue().getAsString());
        }
    }
}
