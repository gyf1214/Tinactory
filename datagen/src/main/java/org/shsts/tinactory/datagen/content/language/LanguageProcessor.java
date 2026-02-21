package org.shsts.tinactory.datagen.content.language;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final String MISSING_TR = "<MISSING TRANSLATE>";
    private static final String MISSING_WORD = "<MISSING WORD>";

    private record Processor(Pattern pattern, Function<Matcher, String> func) {}

    private final String splitter;
    private final boolean isEnglish;
    private final Map<String, String> words = new HashMap<>();
    private final List<Processor> processors = new ArrayList<>();
    private final Map<String, String> extras = new HashMap<>();

    public LanguageProcessor(String locale, String splitter) {
        this.splitter = splitter;
        this.isEnglish = locale.equals("en_us");
    }

    public void word(String key, String val) {
        words.put(key, val);
    }

    public void extra(String key, String val) {
        extras.put(key, val);
    }

    private String normalize(String str) {
        if (words.containsKey(str)) {
            return words.get(str);
        }
        if (!isEnglish) {
            return MISSING_WORD;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private String normalize(Matcher matcher, int group) {
        var key = matcher.group(group);
        if (words.containsKey(key)) {
            return words.get(key);
        }
        return Arrays.stream(key.split("_"))
            .map(this::normalize)
            .collect(Collectors.joining(splitter));
    }

    public void pattern(String pattern, String val) {
        var pattern1 = Pattern.compile("^" + pattern + "$");

        var groupPat = Pattern.compile("\\$[0-9]+");
        var groupMatcher = groupPat.matcher(val);
        var groups = new ArrayList<Function<Matcher, String>>();

        var sb = new StringBuilder();
        var lastPos = 0;
        while (groupMatcher.find()) {
            sb.append(val, lastPos, groupMatcher.start());
            sb.append("%s");
            lastPos = groupMatcher.end();

            var index = Integer.parseInt(groupMatcher.group().substring(1));
            groups.add(matcher -> normalize(matcher, index));
        }
        sb.append(val, lastPos, val.length());
        var fmt = sb.toString();

        processors.add(new Processor(pattern1, matcher -> fmt.formatted(
            groups.stream().map($ -> $.apply(matcher)).toArray())));
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

    public void process(Set<String> keys, LanguageProvider prov, Consumer<String> onProcess) {
        for (var key : keys) {
            if (extras.containsKey(key)) {
                onProcess.accept(key);
                continue;
            }
            var value = process(key);
            if (value.isPresent()) {
                onProcess.accept(key);
            }
            prov.add(key, value.orElse(MISSING_TR));
        }
        for (var entry : extras.entrySet()) {
            prov.add(entry.getKey(), entry.getValue());
        }
    }
}
