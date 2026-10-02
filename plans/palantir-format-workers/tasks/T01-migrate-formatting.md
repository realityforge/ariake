# T01 — Migrate formatting enforcement

- Status: pending
- Blocked by: None
- Spec coverage: R1–R5, AC1–AC5

## Delivers

Public module worker checks through the existing CI gate, developer write/watch wrappers, removal of local formatter dependency plumbing, reviewed/assigned auto-merge PR and auditable closeout.

## Acceptance criteria

- [ ] Pin verified release and enable workers; all current graph-owned Java sources appear in actions.
- [ ] Dirty tracked Java fails check without source/index mutation and displays write remediation; write repairs it.
- [ ] Public write/watch modes and invalid-mode handling verified; local dependency plumbing removed and generation stable.
- [ ] Buildifier and tools/check.sh pass, product dependency pins unchanged, no formatting churn.
- [ ] Read-only planning/implementation gates pass; plan and implementation evidence committed, plan removed at closeout; PR attached, assigned and auto-merge state verified.

## Validation

Release checksum; Bazel aquery input/JavaInfo graph audit; profile/execution/worker log reuse evidence; source/index hash negative probe and write repair; shell syntax/usage/public watcher smoke check; tools/update_java_deps.sh regeneration stability; tools/check.sh; final diff review; GitHub PR state readback.

## Evidence

pending
