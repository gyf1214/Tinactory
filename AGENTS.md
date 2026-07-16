# AGENTS

## !!IMPORTANT NOTES!!

- NEVER use fully qualified class names in Java/Kotlin code, ALWAYS import types.

## Build

- Use `./gradlew runData` to generate resources for `mod`.
- Before `runData`, ensure `libs/tinactory_extra_resources_{version}.zip` exists (current:
  `libs/tinactory_extra_resources_v4.zip`).
- Run `runData` once before testing/packaging, or whenever `datagen` changes.
- NeoForge's merged JAR is generated at `mod/build/moddev/artifacts/neoforge-{version}-merged.jar` and its source JAR
  at `mod/build/moddev/artifacts/neoforge-{version}-sources.jar`.

## Unit Test

- Unit Test only applies to `api.*` and `core.*` under `mod/src/main`.
- Never add unit test for `datagen` or `mod/src/test` itself, including `check.*` and `gametest.*`.
- Never bootstrap Minecraft in a unit test.
- Do not add unit tests for `integration.*` and `content.*`.
- When unit test does not apply, use constrained TDD with compile/verification coverage unless the user explicitly asks
  for different test coverage.

## Format Validation

- Before committing any code changes, run `./gradlew checkSource checkstyleMain checkstyleTest` as the required
  format validation task.
- `checkSource` results are in `mod/build/reports/checkSource` and `datagen/build/reports/checkSource`

## Integration Test

- `mod`: run `./gradlew runGameTestServer` (server exits after all tests finish).
- `datagen`: `./gradlew runData` serves as the test. No separate test for `datagen`.

## Coding Rules

### Common

- Use LF (`\n`) line endings.
- Keep at most 1 blank line in code/declarations.
- Keep 0 blank lines before closing braces.
- Continuation indent: 4 spaces.
- Avoid multiline alignment.
- Wrap lists/arguments/annotations/enum constants as needed (do not force one-per-line).
- When a call or declaration spans multiple lines, put the closing `)` on the last item line, not on its own line.

### Java

- Avoid wildcard imports.
- Use `var` whenever the inferred type is clear.
- Avoid duplicate type names in the same file/import set.
- Import order: non-`java` imports, blank line, `java.*`, blank line, static imports.
- Keep simple methods, lambdas, and classes on one line when possible.
- Always use braces for `if`, `for`, `while`, and `do-while`.
- Interface names must start with `I`.
- Do not use package-private classes or methods except in test source; always declare their visibility explicitly.
- Use `protected` only to permit subclass access, not merely to allow access from the same package.
- Annotate top-level classes/interfaces/records/enums with `@ParametersAreNonnullByDefault` and
  `@MethodsReturnNonnullByDefault`; no need to annotate inner classes/interfaces/enums/records. Use `@Nullable` for
  nullable fields/overrides.
- Test code does not require `@ParametersAreNonnullByDefault` or `@MethodsReturnNonnullByDefault`.
- Mark singleton/utility classes `final`.

### Kotlin

- Avoid star imports.
- Import order: normal imports, `java.*`, `kotlin.*`, aliased imports.
- Keep parameter parentheses on the same line.
