package org.shsts.tinactory.datagen.quest;

import dev.ftb.mods.ftblibrary.snbt.SNBT;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class QuestTranslationChecker {
    private static final String PROJECT_ROOT_PROPERTY = "tinactory.projectRoot";
    private static final Path PROJECT_ROOT = Path.of(System.getProperty(PROJECT_ROOT_PROPERTY, "."));
    private static final Path CHAPTERS_PATH = PROJECT_ROOT.resolve("extra/ftbquests/quests/chapters");
    private static final Path LANG_PATH = PROJECT_ROOT.resolve("extra/ftbquests/quests/lang");
    private static final Pattern OBJECT_ID = Pattern.compile("[0-9A-F]{16}");
    private static final Pattern TRANSLATION_KEY =
        Pattern.compile("(chapter|quest|task|reward)[.]([0-9A-F]{16})[.]([a-z_]+)");
    private static final Set<String> SUPPORTED_TRANSLATION_KEYS =
        Set.of("title", "quest_subtitle", "quest_desc", "chapter_subtitle");
    private static final Set<String> LOCALES = Set.of("zh_cn", "en_us");
    private static final Set<String> LEGACY_TEXT_FIELDS = Set.of("title", "subtitle", "description");

    private final List<String> errors = new ArrayList<>();
    private final Set<QuestObject> questObjects = new HashSet<>();
    private final Set<String> objectIds = new HashSet<>();
    private final Map<String, Set<String>> localeKeys = new HashMap<>();

    public static void main(String[] args) throws Exception {
        if (args.length != 0) {
            throw new IllegalArgumentException("Usage: QuestTranslationChecker");
        }
        var checker = new QuestTranslationChecker();
        checker.run();
    }

    private void run() throws IOException {
        for (var path : listChapters()) {
            processChapter(path);
        }
        for (var locale : LOCALES) {
            checkLanguage(locale);
        }
        checkLocaleKeys();
        if (!errors.isEmpty()) {
            throw new IllegalStateException(String.join("\n", errors));
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
        var tag = readSnbt(path);
        var context = PROJECT_ROOT.relativize(path).toString();
        registerObject("chapter", tag, context);
        rejectLegacyText(path, tag, "");
        if (tag.contains("quests", Tag.TAG_LIST)) {
            var quests = tag.getList("quests", Tag.TAG_COMPOUND);
            for (var i = 0; i < quests.size(); i++) {
                processQuest(path, quests.getCompound(i), ".quests[" + i + "]");
            }
        }
    }

    private void processQuest(Path path, CompoundTag quest, String pathInFile) {
        registerObject("quest", quest, path + pathInFile);
        processChildren(path, quest, pathInFile, "tasks", "task");
        processChildren(path, quest, pathInFile, "rewards", "reward");
    }

    private void processChildren(Path path, CompoundTag parent, String pathInFile, String listName, String objectType) {
        if (!parent.contains(listName, Tag.TAG_LIST)) {
            return;
        }
        var children = parent.getList(listName, Tag.TAG_COMPOUND);
        for (var i = 0; i < children.size(); i++) {
            var childPath = pathInFile + "." + listName + "[" + i + "]";
            registerObject(objectType, children.getCompound(i), path + childPath);
        }
    }

    private void registerObject(String objectType, CompoundTag tag, String context) {
        if (!tag.contains("id", Tag.TAG_STRING)) {
            errors.add("Missing FTB object id in " + context);
            return;
        }
        var id = tag.getString("id");
        checkObjectId(context, id);
        if (!objectIds.add(id)) {
            errors.add("Duplicate FTB object id in quest source: " + id);
        }
        questObjects.add(new QuestObject(objectType, id));
    }

    private void checkLanguage(String locale) throws IOException {
        var path = LANG_PATH.resolve(locale + ".snbt");
        var tag = readSnbt(path);
        var keys = new HashSet<String>();
        for (var key : tag.getAllKeys()) {
            checkTranslationKey(locale, key);
            checkTranslationValue(path, key, tag.get(key));
            keys.add(key);
        }
        localeKeys.put(locale, keys);
    }

    private void checkTranslationKey(String locale, String key) {
        var matcher = TRANSLATION_KEY.matcher(key);
        if (!matcher.matches()) {
            errors.add("Unsupported " + locale + " FTB quest translation key shape: " + key);
            return;
        }
        var objectType = matcher.group(1);
        var objectId = matcher.group(2);
        var translationKey = matcher.group(3);
        checkObjectId(locale + ":" + key, objectId);
        if (!SUPPORTED_TRANSLATION_KEYS.contains(translationKey)) {
            errors.add("Unsupported " + locale + " FTB quest translation key: " + key);
        }
        if (!questObjects.contains(new QuestObject(objectType, objectId))) {
            errors.add("Stale " + locale + " FTB quest translation key for missing object: " + key);
        }
    }

    private void checkTranslationValue(Path path, String key, Tag value) {
        if (value instanceof StringTag stringTag) {
            checkNoLegacyPlaceholder(path, "." + key, stringTag.getAsString());
        } else if (value instanceof ListTag list) {
            for (var i = 0; i < list.size(); i++) {
                var child = list.get(i);
                if (child instanceof StringTag stringTag) {
                    checkNoLegacyPlaceholder(path, "." + key + "[" + i + "]", stringTag.getAsString());
                } else {
                    errors.add("FTB quest translation list must contain only strings in " +
                        PROJECT_ROOT.relativize(path) + "." + key + "[" + i + "]");
                }
            }
        } else {
            errors.add("FTB quest translation value must be a string or string list in " +
                PROJECT_ROOT.relativize(path) + "." + key);
        }
    }

    private void checkLocaleKeys() {
        var zhCnKeys = localeKeys.getOrDefault("zh_cn", Set.of());
        var enUsKeys = localeKeys.getOrDefault("en_us", Set.of());
        for (var key : zhCnKeys) {
            if (!enUsKeys.contains(key)) {
                errors.add("Missing en_us FTB quest translation key: " + key);
            }
        }
        for (var key : enUsKeys) {
            if (!zhCnKeys.contains(key)) {
                errors.add("Missing zh_cn FTB quest translation key: " + key);
            }
        }
    }

    private void rejectLegacyText(Path path, Tag tag, String pathInFile) {
        if (tag instanceof CompoundTag compound) {
            for (var key : compound.getAllKeys()) {
                var childPath = pathInFile + "." + key;
                if (LEGACY_TEXT_FIELDS.contains(key)) {
                    errors.add("Legacy localized quest field remains in " + PROJECT_ROOT.relativize(path) + childPath);
                }
                var child = compound.get(key);
                if (child != null) {
                    rejectLegacyText(path, child, childPath);
                }
            }
        } else if (tag instanceof ListTag list) {
            for (var i = 0; i < list.size(); i++) {
                rejectLegacyText(path, list.get(i), pathInFile + "[" + i + "]");
            }
        } else if (tag instanceof StringTag stringTag) {
            checkNoLegacyPlaceholder(path, pathInFile, stringTag.getAsString());
        }
    }

    private void checkNoLegacyPlaceholder(Path path, String pathInFile, String value) {
        if (value.contains("tinactory.quests.")) {
            errors.add("Legacy quest placeholder remains in " + PROJECT_ROOT.relativize(path) + pathInFile + ": " +
                value);
        }
    }

    private void checkObjectId(String context, String id) {
        if (!OBJECT_ID.matcher(id).matches()) {
            errors.add("Invalid FTB quest object id in " + context + ": " + id);
        } else if (id.charAt(0) > '7') {
            errors.add("FTB quest object id exceeds signed long range in " + context + ": " + id);
        }
    }

    private static CompoundTag readSnbt(Path path) throws IOException {
        var tag = SNBT.read(path);
        if (tag == null) {
            throw new IOException("Failed to read FTB Quests SNBT: " + path);
        }
        return tag;
    }

    private record QuestObject(String type, String id) {}
}
