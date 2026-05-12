package org.shsts.tinactory.datagen.quest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.ftb.mods.ftblibrary.snbt.SNBT;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

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
    private static final String PROJECT_ROOT_PROPERTY = "tinactory.projectRoot";
    private static final Path PROJECT_ROOT = Path.of(System.getProperty(PROJECT_ROOT_PROPERTY, "."));
    private static final Path CHAPTERS_PATH = PROJECT_ROOT.resolve("extra/ftbquests/quests/chapters");
    private static final Path LANGUAGE_PATH =
        PROJECT_ROOT.resolve("datagen/src/main/resources/meta/tinactory/language");
    private static final Pattern INTERPOLATION = Pattern.compile("\\{(tinactory[.]quests[.][^}]+)}");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final List<String> CHAPTER_ORDER = List.of("id", "group", "order_index", "filename", "title", "icon",
        "tags", "disable_toast", "subtitle", "always_invisible", "default_quest_shape",
        "default_hide_dependency_lines", "images", "default_min_width", "progression_mode",
        "hide_quest_details_until_startable", "quests", "quest_links");
    private static final List<String> QUEST_ORDER = List.of("title", "icon", "tags", "disable_toast", "x", "y",
        "shape", "subtitle", "description", "guide_page", "hide_dependency_lines", "min_required_dependencies",
        "dependencies", "hide", "dependency_requirement", "hide_text_until_complete", "size", "optional",
        "min_width", "can_repeat", "invisible", "invisible_until_tasks", "ignore_reward_blocking",
        "progression_mode", "hide_details_until_startable", "id", "tasks", "rewards");
    private static final List<String> TASK_ORDER = List.of("id", "type", "title", "icon", "tags", "disable_toast",
        "item", "count", "consume_items", "only_from_crafting", "match_nbt", "weak_nbt_match", "task_screen_only");
    private static final List<String> REWARD_ORDER = List.of("id", "type", "title", "icon", "tags", "team_reward",
        "auto", "exclude_from_claim_all", "ignore_reward_blocking", "item", "count", "random_bonus", "only_one");
    private static final List<String> ITEM_STACK_ORDER = List.of("id", "Count", "tag");
    private static final Set<String> BOOLEAN_FIELDS = Set.of("default_hide_dependency_lines", "always_invisible",
        "hide_quest_details_until_startable", "disable_toast", "hide_dependency_lines", "hide",
        "hide_text_until_complete", "optional", "can_repeat", "invisible", "ignore_reward_blocking",
        "hide_details_until_startable", "consume_items", "only_from_crafting", "match_nbt", "weak_nbt_match",
        "task_screen_only", "team_reward", "exclude_from_claim_all", "only_one");

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

    private void run() throws IOException {
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

    private void processChapter(Path path) throws IOException {
        var fileName = path.getFileName().toString();
        var chapter = fileName.substring(0, fileName.length() - ".snbt".length());
        var rootKey = "tinactory.quests." + chapter;
        var tag = readSnbt(path);
        processString(tag, "title", rootKey + ".title");
        processStringOrList(tag, "subtitle", rootKey + ".subtitle");
        if (tag.contains("quests", Tag.TAG_LIST)) {
            var quests = tag.getList("quests", Tag.TAG_COMPOUND);
            for (var i = 0; i < quests.size(); i++) {
                processQuest(quests.getCompound(i), rootKey + "." + "%03d".formatted(i));
            }
        }
        if (write) {
            writeSnbt(path, orderChapter(tag));
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

    private static CompoundTag readSnbt(Path path) throws IOException {
        var tag = SNBT.read(path);
        if (tag == null) {
            throw new IOException("Failed to read FTB Quests SNBT: " + path);
        }
        return tag;
    }

    private static void writeSnbt(Path path, CompoundTag tag) throws IOException {
        if (!SNBT.write(path, tag)) {
            throw new IOException("Failed to write FTB Quests SNBT: " + path);
        }
    }

    private static CompoundTag orderChapter(CompoundTag tag) {
        return orderCompound(tag, CHAPTER_ORDER);
    }

    private static Tag orderTag(String key, Tag tag) {
        if (tag instanceof CompoundTag compound) {
            if (key.equals("quests")) {
                return orderCompound(compound, QUEST_ORDER);
            } else if (key.equals("tasks")) {
                return orderCompound(compound, TASK_ORDER);
            } else if (key.equals("rewards")) {
                return orderCompound(compound, REWARD_ORDER);
            } else if (compound.contains("id", Tag.TAG_STRING) && compound.contains("Count", Tag.TAG_BYTE)) {
                return orderCompound(compound, ITEM_STACK_ORDER);
            }
            return orderCompound(compound, List.of());
        } else if (tag instanceof ListTag list) {
            var ordered = new ListTag();
            for (var value : list) {
                ordered.add(orderTag(key, value));
            }
            return ordered;
        }
        return tag;
    }

    private static CompoundTag orderCompound(CompoundTag tag, List<String> order) {
        var ret = new SNBTCompoundTag();
        for (var key : order) {
            putOrdered(ret, tag, key);
        }
        for (var key : tag.getAllKeys()) {
            if (!order.contains(key)) {
                putOrdered(ret, tag, key);
            }
        }
        return ret;
    }

    private static void putOrdered(CompoundTag ret, CompoundTag tag, String key) {
        if (!tag.contains(key)) {
            return;
        }
        var value = tag.get(key);
        if (BOOLEAN_FIELDS.contains(key) && value.getId() == Tag.TAG_BYTE) {
            ret.putBoolean(key, tag.getBoolean(key));
        } else {
            ret.put(key, orderTag(key, value));
        }
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
