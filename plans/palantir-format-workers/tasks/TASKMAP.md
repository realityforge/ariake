# Task Map

- Spec: [SPEC.md](../SPEC.md)
- Status: planned
- Current frontier: T01
- Planning reviewer: /root/planning_review (2/3 rounds), Findings: none; delivery ownership/merge authority confirmed
- Plan checkpoint: automatic; completed evidence-based grill tree, explicit user gate exception and passing planning review
- Implementation reviewer: pending (0/5 rounds)
- Authority: completed evidence-based design tree and explicit user gate exception.

## Full-scope validation

- Gate: tools/check.sh plus action-input audit, dirty-source/index safety and worker reuse evidence
- Evidence: pending

## Tasks

| ID | Task | Status | Blocked by |
| --- | --- | --- | --- |
| T01 | [Migrate formatting enforcement](T01-migrate-formatting.md) | pending | None |

## Sequencing notes

One atomic migration; publish only after implementation review and committed closeout. Assignment and auto-merge are explicitly authorized; no forced merge.

## Workflow-owned delivery (R5 / AC5)

- Planning: completed design tree, same read-only reviewer, committed approved plan.
- Implementation: fresh read-only reviewer, evidence and review-state commits before closeout.
- Closeout: validate cleanup gate, delete exact plan tree and commit deletion.
- Publication: create/attach PR, assign realityforge, verify exact-head CI and review state. Native auto-merge first if it can wait; otherwise the subsequent user authorization permits --auto --merge --match-head-commit after all actual checks pass. No --admin or settings changes. Report actual outcome, significant decisions, tests, reviews and audit commits.

## Promoted knowledge

not-required: no docs/adr, glossary, specs or deferred directories.
