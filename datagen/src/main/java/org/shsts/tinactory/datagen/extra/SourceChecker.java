package org.shsts.tinactory.datagen.extra;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SourceChecker {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Path SOURCE = Path.of("../../mod/src/main/java");
    private static final String TOP_PACKAGE = "org.shsts.tinactory";
    private static final Path TOP_FOLDER = SOURCE.resolve(TOP_PACKAGE.replace('.', '/'));

    private static final Multimap<String, String> BAN_LIST = ArrayListMultimap.create();

    private static void checkLine(Path srcFile, String line, Collection<String> bans) {
        String imp;
        if (line.startsWith("import static ")) {
            imp = line.substring(14, line.length() - 1);
        } else if (line.startsWith("import ")) {
            imp = line.substring(7, line.length() - 1);
        } else {
            return;
        }

        for (var ban : bans) {
            if (imp.startsWith(TOP_PACKAGE + "." + ban)) {
                LOGGER.warn("invalid import srcFile={}, import={}", srcFile, imp);
            }
        }
    }

    private static void checkSource(Path srcFile) throws IOException {
        var rel = TOP_FOLDER.relativize(srcFile);
        var pkg = rel.getName(0).toString();
        var bans = BAN_LIST.get(pkg);
        if (bans.isEmpty()) {
            LOGGER.trace("skip src={}", srcFile);
            return;
        }

        try (var lines = Files.lines(srcFile)) {
            lines.forEach(l -> checkLine(rel, l, bans));
        }
    }

    private static void unsafeCheck() throws IOException {
        BAN_LIST.put("api", "core");
        BAN_LIST.put("api", "content");
        BAN_LIST.put("core", "content");

        List<Path> files;
        try (var paths = Files.walk(TOP_FOLDER)) {
            files = paths.filter(Files::isRegularFile).toList();
        }
        for (var file : files) {
            checkSource(file);
        }
    }

    public static void check() {
        try {
            unsafeCheck();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
