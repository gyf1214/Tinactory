package org.shsts.tinactory.check;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CheckSource {
    // Deferred violation:
    // - core.gui.client.ProcessingRecipeBookItem.java -> integration.gui.client.RecipeDisplayRegistry
    private static final int MAX_SOURCE_VIOLATIONS = 1;

    private CheckSource() {}

    public static void main(String[] args) throws IOException {
        var status = run(args, new PrintWriter(System.out, true));
        if (status != 0) {
            System.exit(status);
        }
    }

    static int run(String[] args, PrintWriter output) throws IOException {
        var options = parseArgs(args);
        var sourceRoots = requiredPaths(options, "source-roots");
        var topPackage = required(options, "top-package");
        var reportFile = requiredPath(options, "report-file");

        Files.createDirectories(reportFile.getParent());
        var checker = new SourceBoundaryChecker(sourceRoots.get(0), topPackage, Map.of(
            "api", List.of("core", "integration", "content", "compat"),
            "core", List.of("integration", "content", "compat"),
            "integration", List.of("content", "compat")));
        for (var sourceRoot : sourceRoots.subList(1, sourceRoots.size())) {
            checker.addSourceRoot(sourceRoot);
        }
        int violations;
        try (var writer = Files.newBufferedWriter(reportFile)) {
            violations = checker.check(writer);
        }
        if (violations > MAX_SOURCE_VIOLATIONS) {
            output.printf("Found %d source violation(s). See %s%n", violations, reportFile);
            output.flush();
            return 1;
        } else if (violations > 0) {
            output.printf("Warning: found %d source violation(s). See %s%n", violations, reportFile);
            output.flush();
        }
        return 0;
    }

    private static Map<String, String> parseArgs(String[] args) {
        var ret = new HashMap<String, String>();
        for (var i = 0; i < args.length; i += 2) {
            if (i + 1 >= args.length || !args[i].startsWith("--")) {
                throw new IllegalArgumentException("Expected arguments as --name value pairs");
            }
            ret.put(args[i].substring(2), args[i + 1]);
        }
        return ret;
    }

    private static String required(Map<String, String> options, String key) {
        var value = options.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing --" + key);
        }
        return value;
    }

    private static Path requiredPath(Map<String, String> options, String key) {
        return Path.of(required(options, key));
    }

    private static List<Path> requiredPaths(Map<String, String> options, String key) {
        return Arrays.stream(required(options, key).split(File.pathSeparator))
            .map(Path::of)
            .toList();
    }
}
