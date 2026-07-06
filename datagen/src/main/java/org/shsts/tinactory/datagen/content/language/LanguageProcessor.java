package org.shsts.tinactory.datagen.content.language;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LanguageProcessor {
    private record Processor(Pattern pattern, Function<Matcher, Optional<String>> func) {}

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

    private Optional<String> normalize(String str) {
        if (words.containsKey(str)) {
            return Optional.of(words.get(str));
        }
        return isEnglish ? Optional.of(StringUtils.capitalize(str)) : Optional.empty();
    }

    private Optional<String> normalize(Matcher matcher, int group) {
        var key = matcher.group(group);
        if (words.containsKey(key)) {
            return Optional.of(words.get(key));
        }
        var sb = new StringBuilder();
        var first = true;
        for (var word : key.split("_")) {
            var word1 = normalize(word);
            if (word1.isPresent()) {
                if (!first) {
                    sb.append(splitter);
                } else {
                    first = false;
                }
                sb.append(word1.get());
            } else {
                return Optional.empty();
            }
        }
        return Optional.of(sb.toString());
    }

    private Optional<String> optionalFmt(String fmt, Collection<Optional<String>> args) {
        var argsList = new ArrayList<String>(args.size());
        for (var arg : args) {
            if (arg.isEmpty()) {
                return Optional.empty();
            }
            argsList.add(arg.get());
        }
        return Optional.of(fmt.formatted(argsList.toArray()));
    }

    public void pattern(String pattern, String val) {
        var pattern1 = Pattern.compile("^" + pattern + "$");

        var groupPat = Pattern.compile("\\$[0-9]+");
        var groupMatcher = groupPat.matcher(val);
        var groups = new ArrayList<Function<Matcher, Optional<String>>>();

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

        processors.add(new Processor(pattern1, matcher -> optionalFmt(
            fmt, groups.stream().map($ -> $.apply(matcher)).toList())));
    }

    private Optional<String> process(String key) {
        for (var processor : processors) {
            var matcher = processor.pattern.matcher(key);
            if (matcher.find()) {
                return processor.func.apply(matcher);
            }
        }
        return Optional.empty();
    }

    public void process(Set<String> keys, LanguageProvider prov, Consumer<String> onProcess) {
        for (var key : keys) {
            if (extras.containsKey(key)) {
                prov.add(key, extras.get(key));
                onProcess.accept(key);
                continue;
            }
            var value = process(key);
            if (value.isPresent()) {
                prov.add(key, value.get());
                onProcess.accept(key);
            }
        }
    }
}
