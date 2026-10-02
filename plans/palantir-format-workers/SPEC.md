# Palantir formatting worker migration

## Source and completed design tree

Authority: delegated user request authorizes implementation, commits, publishing, assignment to realityforge and auto-merge. The explicit grill/entry-gate exception permits proceeding when references and repository answer every decision. Subsequent clarification limits this repository to Java reachable through the target graph.

Evidence rounds:
1. Outcome → replace local formatter dependency plumbing with the public rules module and real worker checks; retain existing CI and wrappers. Existing CI calls tools/check.sh, which calls tools/java_format.sh check.
2. Coverage → graph-only, explicitly settled by user. All 66 current Java files are owned by colocated BUILD targets. No GWT/J2CL, generated-fixture formatter consumer, staged-file formatter or ahab configuration exists. Include library exports, coverage_tests, architecture_test, all four example servers and their integration tests; data edges are not traversed by the aspect, so servers need explicit roots.
3. Dependency → latest released module v0.1.1 owns existing formatter 2.93.0. BCR module endpoint returns 404; release archive SHA-256 independently matches sha256-4h/h0cVmPQ/7jkXnvW69rmABbfXOOzoXuBh3gMJDMk8=. Use immutable archive_override. Its MODULE requires rules_java 9.9.0, so minimum-version selection necessarily upgrades the resolved current 9.6.1; verify this required transitive change through the full gate while leaving unrelated pins alone.
4. Commands → keep write default and check modes; add watch mode and thin watch wrapper matching the supplied reference. Public write/watch tools use existing explicit examples, src, tools roots. Check builds the root target and never writes sources/index.
5. Delivery → meaningful graph/input audit, dirty-source failure and remediation, actual worker reuse evidence, dependency regeneration, buildifier and full tools/check.sh; reviewed PR with assignment and auto-merge without bypassing CI.
Frontier: empty. No material user decision remains.

## Problem and required outcome

Formatting currently launches a repository-local formatter process from enumerated files. Move enforcement to Bazel worker actions while keeping developer commands and CI enforcement obvious.

## Scope and constraints

Adopt @rules_palantir_java_format; remove obsolete formatter binary/dependency generation; preserve product dependencies, graph-owned Java coverage and existing CI. No non-graph fallback, unrelated upgrades, formatting churn or new CI. Source directories own their BUILD files; no glob(). Preserve user checkout/index.

## Requirements and acceptance criteria

- R1 / AC1: pinned public module supplies PalantirJavaFormat actions with worker,local strategy and one worker instance; observed worker execution/reuse proves it.
- R2 / AC2: root testonly format target covers graph-owned current Java sources, including example code and architecture tests; audit action inputs and reject an intentionally unformatted owned source without modifying it or the index. Failure gives tools/java_format.sh write; remediation restores a passing check.
- R3 / AC3: write/check/watch wrappers use public tools and existing roots; invalid mode returns 2. Remove obsolete local formatter and depgen references; regenerate MODULE lock and confirm dependency generation is stable.
- R4 / AC4: existing CI retains tools/check.sh gate; full gate passes, buildifier passes, final diff has no unrelated upgrades/churn. If environmental issues arise, investigate and resolve safely before claiming completion.
- R5 / AC5: committed plan, implementation/evidence and closeout deletion; planning and fresh implementation read-only reviewers pass. Publish/attach PR, assign realityforge and enable allowed auto-merge; report any settings blocker accurately.

## Significant decisions

| Decision | Rationale | Impact | User verification |
| --- | --- | --- | --- |
| Immutable v0.1.1 override | BCR has no module; checked release integrity | reproducible dependency, no formatter upgrade | pin/integrity in MODULE |
| Explicit graph roots, testonly target | aspect traverses deps/runtime_deps/exports/tests, not data | includes all 66 current sources; future graph roots must be included | root target and input audit |
| Existing write roots and wrapper conventions | requested references and current source layout | convenient write/watch; check scope graph-only | wrapper modes/remediation |
| Accept required rules_java 9.9.0 resolution | v0.1.1 declares that minimum | necessary transitive change; full-gate verification | lockfile/module graph |
| Preserve CI workflow | already calls required gate | no additional CI machinery | existing check.sh chain |

## Technical and testing decisions

Root public check rule, two mnemonic-specific .bazelrc settings, external public write/watch executables. One atomic migration task avoids maintaining two formatter paths. Use aquery to compare check-action inputs with JavaInfo-owned workspace sources, execution log/profile/worker logs to prove worker execution and reuse, reversible tracked-source negative probe with source/index hashes, wrapper write recovery, full tools/check.sh and generated-file stability. Review before final closeout and publishing.

## Open questions

None.
