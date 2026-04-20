package org.shsts.tinactory.check;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CheckSource {
    private CheckSource() {}

    public static void main(String[] args) throws IOException {
        var status = run(args, new PrintWriter(System.out, true));
        if (status != 0) {
            System.exit(status);
        }
    }

    static int run(String[] args, PrintWriter output) throws IOException {
        var options = parseArgs(args);
        var sourceRoot = requiredPath(options, "source-root");
        var topPackage = required(options, "top-package");
        var reportFile = requiredPath(options, "report-file");

        Files.createDirectories(reportFile.getParent());
        var checker = new SourceBoundaryChecker(sourceRoot, topPackage, Map.of(
            "api", List.of("core", "content"),
            "core", List.of("content")));
        int violations;
        try (var writer = Files.newBufferedWriter(reportFile)) {
            violations = checker.check(writer);
        }
        if (violations > 0) {
            output.printf("Found %d source violation(s). See %s%n", violations, reportFile);
            output.flush();
            return 1;
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
}
