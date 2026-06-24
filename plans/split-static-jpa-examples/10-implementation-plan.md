# Split Static and JPA Examples Implementation Plan

Status: review requested; implementation proceeding.

## Phase Sequence

1. Record the split requirements and task board.
2. Remove JPA concerns from `examples/static`.
3. Add `examples/jpa` with its own entity, service, application bootstrap, resources, and binary.
4. Add jar-launch integration tests for both examples.
5. Run targeted checks and the required `tools/check.sh` gate.

## Delivery Approach

- Move behavior by copying JPA-specific code into a new package, then deleting JPA wiring from static.
- Keep static content files and Brotli build actions in `examples/static/content`.
- Use temporary directories in integration tests for static roots, JPA DB files, and config files.
- Use deploy jars in test `data` and resolve them from Bazel runfiles.

## High-Risk Areas

- Runfiles path resolution for implicit deploy jar outputs.
  - Mitigation: add a small runfiles resolver in the integration test and validate with `bazel test`.
- External process startup timing.
  - Mitigation: parse the startup line and fail with captured process output if startup times out.
- H2 database files from tests.
  - Mitigation: point the JPA example at a test temp directory.

## Required Full Gate

```bash
tools/check.sh
```

## Decision Log

- Q-01: Use compiled deploy jars because the user explicitly asked to start the program using the compiled jar.
