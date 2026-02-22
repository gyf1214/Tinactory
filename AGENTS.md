# AGENTS

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
