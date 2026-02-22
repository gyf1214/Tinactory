# AGENTS

## Build
- Use `./gradlew runData` to generate resources for `mod`.
- Before `runData`, ensure `libs/tinactory_extra_resources_{version}.zip` exists (current: `libs/tinactory_extra_resources_v1.zip`).
- Run `runData` once before testing/packaging, or whenever `datagen` changes.

## Test
- `mod`: run `./gradlew runGameTestServer` (server exits after all tests finish).
- `datagen`: `./gradlew runData` serves as the test. No separate test for `datagen`.

## Format
Common
- Use LF (`\n`) line endings.
- Keep at most 1 blank line in code/declarations.
- Keep 0 blank lines before closing braces.
- Continuation indent: 4 spaces.
- Avoid multiline alignment.
- Wrap lists/arguments/annotations/enum constants as needed (do not force one-per-line).

### Java
- Avoid wildcard imports.
- Import order: non-`java` imports, blank line, `java.*`, blank line, static imports.
- Keep simple methods, lambdas, and classes on one line when possible.
- Always use braces for `if`, `for`, `while`, and `do-while`.

### Kotlin
- Avoid star imports.
- Import order: normal imports, `java.*`, `kotlin.*`, aliased imports.
- Keep parameter parentheses on the same line.
