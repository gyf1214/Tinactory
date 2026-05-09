package org.shsts.tinactory.datagen.quest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class QuestLanguageExtractor {
    private static final Path CHAPTERS_PATH = Path.of("extra/ftbquests/quests/chapters");
    private static final Path LANGUAGE_PATH = Path.of("datagen/src/main/resources/meta/tinactory/language");
    private static final Pattern INTERPOLATION = Pattern.compile("\\{(tinactory[.]quests[.][^}]+)}");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final boolean write;
    private final JsonObject zhCn;
    private final JsonObject enUs;
    private final JsonObject zhCnQuests;
    private final JsonObject enUsQuests;
    private final Set<String> keys = new LinkedHashSet<>();
    private final List<String> errors = new ArrayList<>();

    private QuestLanguageExtractor(boolean write) throws IOException {
        this.write = write;
        this.zhCn = readLanguage("zh_cn");
        this.enUs = readLanguage("en_us");
        this.zhCnQuests = getOrCreateQuests(zhCn);
        this.enUsQuests = getOrCreateQuests(enUs);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1 || (!args[0].equals("extract") && !args[0].equals("check"))) {
            throw new IllegalArgumentException("Usage: QuestLanguageExtractor <extract|check>");
        }
        var extractor = new QuestLanguageExtractor(args[0].equals("extract"));
        extractor.run();
    }

    private void run() throws IOException, CommandSyntaxException {
        var chapters = listChapters();
        for (var chapterPath : chapters) {
            processChapter(chapterPath);
        }
        checkLocales();
        if (!errors.isEmpty()) {
            throw new IllegalStateException(String.join("\n", errors));
        }
        if (write) {
            writeLanguage("zh_cn", zhCn);
            writeLanguage("en_us", enUs);
        }
    }

    private List<Path> listChapters() throws IOException {
        try (var stream = Files.list(CHAPTERS_PATH)) {
            return stream
                .filter(path -> path.getFileName().toString().endsWith(".snbt"))
                .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                .toList();
        }
    }

    private void processChapter(Path path) throws IOException, CommandSyntaxException {
        var fileName = path.getFileName().toString();
        var chapter = fileName.substring(0, fileName.length() - ".snbt".length());
        var rootKey = "tinactory.quests." + chapter;
        var tag = TagParser.parseTag(normalizeSnbt(Files.readString(path, StandardCharsets.UTF_8)));
        processString(tag, "title", rootKey + ".title");
        processStringOrList(tag, "subtitle", rootKey + ".subtitle");
        if (tag.contains("quests", Tag.TAG_LIST)) {
            var quests = tag.getList("quests", Tag.TAG_COMPOUND);
            for (var i = 0; i < quests.size(); i++) {
                processQuest(quests.getCompound(i), rootKey + "." + "%03d".formatted(i));
            }
        }
        if (write) {
            Files.writeString(path, tag + "\n", StandardCharsets.UTF_8);
        }
    }

    private void processQuest(CompoundTag quest, String questKey) {
        processString(quest, "title", questKey + ".title");
        processString(quest, "subtitle", questKey + ".subtitle");
        processStringOrList(quest, "description", questKey + ".description");
        processChildren(quest, "tasks", questKey + ".task");
        processChildren(quest, "rewards", questKey + ".reward");
    }

    private void processChildren(CompoundTag parent, String childName, String key) {
        if (!parent.contains(childName, Tag.TAG_LIST)) {
            return;
        }
        var children = parent.getList(childName, Tag.TAG_COMPOUND);
        for (var i = 0; i < children.size(); i++) {
            var childKey = key + "." + "%02d".formatted(i);
            var child = children.getCompound(i);
            processString(child, "title", childKey + ".title");
            processString(child, "subtitle", childKey + ".subtitle");
            processStringOrList(child, "description", childKey + ".description");
        }
    }

    private void processStringOrList(CompoundTag tag, String field, String key) {
        if (tag.contains(field, Tag.TAG_STRING)) {
            processString(tag, field, key);
        } else if (tag.contains(field, Tag.TAG_LIST)) {
            processStringList(tag.getList(field, Tag.TAG_STRING), key);
        }
    }

    private void processString(CompoundTag tag, String field, String key) {
        if (!tag.contains(field, Tag.TAG_STRING)) {
            return;
        }
        var value = tag.getString(field);
        var replacement = processValue(value, key);
        replacement.ifPresent(newValue -> tag.putString(field, newValue));
    }

    private void processStringList(ListTag list, String key) {
        var textIndex = 0;
        for (var i = 0; i < list.size(); i++) {
            var value = list.getString(i);
            if (value.isBlank()) {
                continue;
            }
            var replacement = processValue(value, key + "." + textIndex);
            if (replacement.isPresent()) {
                list.set(i, StringTag.valueOf(replacement.get()));
            }
            textIndex++;
        }
    }

    private Optional<String> processValue(String value, String key) {
        if (value.isBlank()) {
            return Optional.empty();
        }
        var matcher = INTERPOLATION.matcher(value);
        if (matcher.matches()) {
            keys.add(matcher.group(1));
            return Optional.empty();
        }
        if (!write) {
            errors.add("Raw quest text remains for " + key + ": " + value);
            return Optional.empty();
        }
        addLocaleEntry(key, value);
        keys.add(key);
        return Optional.of("{" + key + "}");
    }

    private void addLocaleEntry(String key, String value) {
        if (zhCnQuests.has(key)) {
            var existing = zhCnQuests.get(key).getAsString();
            if (!existing.equals(value)) {
                errors.add("Duplicate quest key has different zh_cn text: " + key);
            }
        } else {
            zhCnQuests.addProperty(key, value);
        }
        if (!enUsQuests.has(key)) {
            enUsQuests.addProperty(key, "");
        }
    }

    private void checkLocales() {
        checkLocale("zh_cn", zhCnQuests);
        checkLocale("en_us", enUsQuests);
    }

    private void checkLocale(String locale, JsonObject quests) {
        for (var key : keys) {
            if (!quests.has(key)) {
                errors.add("Missing " + locale + " quest key: " + key);
            }
        }
        for (var entry : quests.entrySet()) {
            if (!keys.contains(entry.getKey())) {
                errors.add("Stale " + locale + " quest key: " + entry.getKey());
            }
        }
    }

    private static JsonObject getOrCreateQuests(JsonObject jo) {
        if (!jo.has("quests")) {
            jo.add("quests", new JsonObject());
        }
        return jo.getAsJsonObject("quests");
    }

    private static JsonObject readLanguage(String locale) throws IOException {
        try (Reader reader = Files.newBufferedReader(languageFile(locale), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static String normalizeSnbt(String snbt) {
        snbt = snbt.replaceAll("list<[^>]+>\\[[0-9]+] ", "");
        var lines = snbt.split("\\R", -1);
        var ret = new StringBuilder();
        for (var i = 0; i < lines.length; i++) {
            var line = lines[i];
            ret.append(line);
            if (needsComma(line, nextNonBlank(lines, i + 1))) {
                ret.append(",");
            }
            if (i + 1 < lines.length) {
                ret.append("\n");
            }
        }
        return ret.toString();
    }

    private static String nextNonBlank(String[] lines, int start) {
        for (var i = start; i < lines.length; i++) {
            var trimmed = lines[i].trim();
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }
        return "";
    }

    private static boolean needsComma(String line, String next) {
        var trimmed = line.trim();
        if (trimmed.isEmpty() || next.isEmpty()) {
            return false;
        }
        var last = trimmed.charAt(trimmed.length() - 1);
        if (last == ',' || last == '{' || last == '[' || next.charAt(0) == '}' || next.charAt(0) == ']') {
            return false;
        }
        return true;
    }

    private static void writeLanguage(String locale, JsonObject jo) throws IOException {
        try (Writer writer = Files.newBufferedWriter(languageFile(locale), StandardCharsets.UTF_8)) {
            GSON.toJson(jo, writer);
            writer.write("\n");
        }
    }

    private static Path languageFile(String locale) {
        return LANGUAGE_PATH.resolve(locale + ".json");
    }
}
