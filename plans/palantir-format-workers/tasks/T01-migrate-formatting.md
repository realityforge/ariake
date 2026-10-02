# T01 — Migrate formatting enforcement

- Status: complete
- Blocked by: None
- Spec coverage: R1–R4, AC1–AC4; prepares R5/AC5 for workflow-managed review and closeout

## Delivers

Public module worker checks through the existing CI gate, developer write/watch wrappers, removal of local formatter dependency plumbing, an implementation branch ready for workflow-managed review, closeout and assigned auto-merge publication.

## Acceptance criteria

- [x] Pin verified release and enable workers; all current graph-owned Java sources appear in actions.
- [x] Dirty tracked Java fails check without source/index mutation and displays write remediation; write repairs it.
- [x] Public write/watch modes and invalid-mode handling verified; local dependency plumbing removed and generation stable.
- [x] Buildifier and tools/check.sh pass, product dependency pins unchanged, no formatting churn.
- [x] Implementation and full-gate evidence prepared for the fresh implementation reviewer; no unrelated worktree changes. Review, closeout and publication are tracked in TASKMAP and do not falsely gate pre-review task completion.

## Validation

Release checksum; Bazel aquery input/JavaInfo graph audit; profile/execution/worker log reuse evidence; source/index hash negative probe and write repair; shell syntax/usage/public watcher smoke check; tools/update_java_deps.sh regeneration stability; tools/check.sh; final diff review; GitHub PR state readback.

## Evidence

See [EVIDENCE.md](../EVIDENCE.md): verified release integrity; 23 successful worker actions cover all 66 Java files; staged/source safety and remediation; write/watch behavior; full tools/check.sh pass with 12 tests, 97.80% line / 100% branch coverage; repeat dependency regeneration stable; final diff clean. Workflow delivery follows task completion.
