package org.shsts.tinactory.check;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SourceBoundaryChecker {
    private static final Pattern FULLY_QUALIFIED_NAME = Pattern.compile(
        "\\b(?:[a-z_][\\w]*\\.){2,}[A-Z][\\w]*(?:\\.[A-Z][\\w]*)*\\b");
    private final List<Path> packageRoots = new ArrayList<>();
    private final String topPackage;
    private final Map<String, List<String>> banList;

    public SourceBoundaryChecker(Path sourceRoot, String topPackage, Map<String, List<String>> banList) {
        this.topPackage = topPackage;
        this.banList = banList;
        addSourceRoot(sourceRoot);
    }

    public void addSourceRoot(Path sourceRoot) {
        packageRoots.add(sourceRoot.resolve(topPackage.replace('.', '/')));
    }

    public int check(Writer writer) throws IOException {
        var violations = 0;
        for (var packageRoot : packageRoots) {
            if (!Files.exists(packageRoot)) {
                continue;
            }
            List<Path> files;
            try (var paths = Files.walk(packageRoot)) {
                files = paths.filter(Files::isRegularFile)
                    .filter(SourceBoundaryChecker::isSourceFile)
                    .toList();
            }
            for (var file : files) {
                violations += checkSource(packageRoot, file, writer);
            }
        }
        writer.flush();
        return violations;
    }

    private int checkSource(Path packageRoot, Path srcFile, Writer writer) throws IOException {
        var rel = packageRoot.relativize(srcFile);
        var pkg = rel.getName(0).toString();
        var bans = banList.get(pkg);
        var violations = 0;
        var stripState = new StripState(false, false);
        var lineNumber = 0;
        try (var lines = Files.lines(srcFile)) {
            for (var line : lines.toList()) {
                lineNumber++;
                if (bans != null && !bans.isEmpty()) {
                    violations += checkImport(rel, line, bans, writer);
                }
                var stripped = stripIgnoredText(line, stripState);
                stripState = stripped.state();
                violations += checkFullyQualifiedName(rel, lineNumber, stripped.line(), writer);
            }
        }
        return violations;
    }

    private int checkImport(Path srcFile, String line, Collection<String> bans, Writer writer) throws IOException {
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

    private int checkFullyQualifiedName(Path srcFile, int lineNumber, String line, Writer writer) throws IOException {
        if (line.startsWith("package ") || line.startsWith("import ")) {
            return 0;
        }
        var violations = 0;
        var matcher = FULLY_QUALIFIED_NAME.matcher(line);
        while (matcher.find()) {
            var name = matcher.group();
            writer.write("%s:%d: fully qualified name %s%n".formatted(srcFile, lineNumber, name));
            violations++;
        }
        return violations;
    }

    private static boolean isSourceFile(Path file) {
        var name = file.getFileName().toString();
        return name.endsWith(".java") || name.endsWith(".kt");
    }

    private static StrippedLine stripIgnoredText(String line, StripState state) {
        var ret = new StringBuilder();
        var i = 0;
        var inString = false;
        var inChar = false;
        var inBlockComment = state.inBlockComment();
        var inTextBlock = state.inTextBlock();
        while (i < line.length()) {
            var ch = line.charAt(i);
            var next = i + 1 < line.length() ? line.charAt(i + 1) : '\0';
            if (inTextBlock) {
                if (startsWith(line, i, "\"\"\"")) {
                    inTextBlock = false;
                    ret.append("   ");
                    i += 3;
                } else {
                    ret.append(' ');
                    i++;
                }
            } else if (inBlockComment) {
                if (ch == '*' && next == '/') {
                    inBlockComment = false;
                    ret.append("  ");
                    i += 2;
                } else {
                    ret.append(' ');
                    i++;
                }
            } else if (inString) {
                if (ch == '\\' && next != '\0') {
                    ret.append("  ");
                    i += 2;
                } else {
                    inString = ch != '"';
                    ret.append(' ');
                    i++;
                }
            } else if (inChar) {
                if (ch == '\\' && next != '\0') {
                    ret.append("  ");
                    i += 2;
                } else {
                    inChar = ch != '\'';
                    ret.append(' ');
                    i++;
                }
            } else if (ch == '/' && next == '/') {
                ret.append(" ".repeat(line.length() - i));
                break;
            } else if (ch == '/' && next == '*') {
                inBlockComment = true;
                ret.append("  ");
                i += 2;
            } else if (startsWith(line, i, "\"\"\"")) {
                inTextBlock = true;
                ret.append("   ");
                i += 3;
            } else if (ch == '"') {
                inString = true;
                ret.append(' ');
                i++;
            } else if (ch == '\'') {
                inChar = true;
                ret.append(' ');
                i++;
            } else {
                ret.append(ch);
                i++;
            }
        }
        return new StrippedLine(ret.toString().stripLeading(), new StripState(inBlockComment, inTextBlock));
    }

    private static boolean startsWith(String line, int offset, String value) {
        return line.regionMatches(offset, value, 0, value.length());
    }

    private record StripState(boolean inBlockComment, boolean inTextBlock) {}

    private record StrippedLine(String line, StripState state) {}
}
