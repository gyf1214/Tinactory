package org.shsts.tinactory.check;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SourceBoundaryChecker {
    private final Path packageRoot;
    private final String topPackage;
    private final Map<String, List<String>> banList;

    public SourceBoundaryChecker(Path sourceRoot, String topPackage, Map<String, List<String>> banList) {
        this.packageRoot = sourceRoot.resolve(topPackage.replace('.', '/'));
        this.topPackage = topPackage;
        this.banList = banList;
    }

    public int check(Writer writer) throws IOException {
        var violations = 0;
        List<Path> files;
        try (var paths = Files.walk(packageRoot)) {
            files = paths.filter(Files::isRegularFile).toList();
        }
        for (var file : files) {
            violations += checkSource(file, writer);
        }
        writer.flush();
        return violations;
    }

    private int checkSource(Path srcFile, Writer writer) throws IOException {
        var rel = packageRoot.relativize(srcFile);
        var pkg = rel.getName(0).toString();
        var bans = banList.get(pkg);
        if (bans == null || bans.isEmpty()) {
            return 0;
        }
        var violations = 0;
        try (var lines = Files.lines(srcFile)) {
            for (var line : lines.toList()) {
                violations += checkLine(rel, line, bans, writer);
            }
        }
        return violations;
    }

    private int checkLine(Path srcFile, String line, Collection<String> bans, Writer writer) throws IOException {
        String imp;
        if (line.startsWith("import static ")) {
            imp = line.substring(14, line.length() - 1);
        } else if (line.startsWith("import ")) {
            imp = line.substring(7, line.length() - 1);
        } else {
            return 0;
        }
        for (var ban : bans) {
            if (imp.startsWith(topPackage + "." + ban)) {
                writer.write("%s: import %s%n".formatted(srcFile, imp));
                return 1;
            }
        }
        return 0;
    }
}
