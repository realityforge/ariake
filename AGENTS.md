# Ariake Agent Rules

- Non-negotiable: do not use `glob()` in Bazel targets; list source files explicitly.
- Non-negotiable: every Java source directory owns its own `BUILD.bazel`; Bazel targets must not list source files from child, sibling, or parent directories.
- Non-negotiable: run `tools/check.sh` before claiming implementation work is complete.
