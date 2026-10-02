# Verification evidence

Environment: macOS arm64, Bazel 9.1.1, repository remote JDK25. User ~/.bazelrc sets shared output_base=/Users/peter/.bazel, which collided with concurrent repository work. All meaningful verification uses a temporary PATH shim adding --output_base=/tmp/ariake-pjf-output; repository and user configuration unchanged. Commands below otherwise use checked-in scripts/options.

## Passed focused checks

- Release archive downloaded from v0.1.1 URL; openssl SHA256/base64 is 4h/h0cVmPQ/7jkXnvW69rmABbfXOOzoXuBh3gMJDMk8=. BCR version endpoint 404. Module minimum rules_java 9.9.0 rechecked from release MODULE.
- bazel run //:buildifier -- MODULE.bazel BUILD.bazel passed. bash -n checked formatter/watch/update/check scripts; invalid mode exits 2 with updated usage.
- bazel build //:java_format_check --worker_verbose --execution_log_json_file=/tmp/ariake-pjf-evidence/execution.json --profile=/tmp/ariake-pjf-evidence/profile.json.gz passed. Execution log has 23 PalantirJavaFormat entries, all runner=worker and exitCode=0. Build log creates exactly one PalantirJavaFormat worker (id 4), reused for all 23 actions.
- bazel aquery 'mnemonic("PalantirJavaFormat", deps(//:java_format_check))' --include_aspects --output=jsonproto confirms 23 worker-capable actions. Union of Java inputs equals all 66 tracked current Java sources exactly, with no generated/external Java. Same union checked against actual execution-log inputs. Architecture tests and four example servers/tests included; no GWT/J2CL targets exist.
- Intentionally inserted extra spaces into package declarations in architecture/package-info.java and example health/package-info.java, staged those experimental edits, and ran tools/java_format.sh check. Nonzero result names both paths and gives tools/java_format.sh write. Source bytes and staged binary diff stayed identical. tools/java_format.sh write restored both originals exactly and left staged content unchanged; next check passed. Experimental index edits restored to HEAD and no Java diff remains.
- tools/java_format_watch.sh started public external watcher with examples/src/tools roots. Modified health package declaration; watcher logged Formatted and restored exact original bytes. Watch process terminated, source restored; no Java diff remains.

## Full gate

tools/check.sh passed (exit 0) after resumption: dependency generation, buildifier_check, explicit format build, bazel build //..., all 12 tests and 6 coverage tests. Line coverage 97.80% (89/91), branch coverage 100.00% (8/8). Earlier standalone bazel test //... also passed all 12 tests. First full-gate attempt failed due No space left on device during Maven source-jar download; the user paused the retry. On resume df showed 3.7GiB available; full gate then passed without repository changes for the environmental failure.

Repeated tools/update_java_deps.sh passed and SHA256 values of MODULE.bazel, MODULE.bazel.lock and third_party/java/BUILD.bazel stayed identical. git diff for third_party/java and Java sources is empty; application pins and source formatting unchanged. Final author diff check passed. No domain promotion required (no domain directories).

Fresh implementation reviewer /root/implementation_review passed round 1/5 with Findings: none. Checked complete implementation/plan alignment, CI wiring, public aspect, exact input/execution evidence, negative/index/write/watch behavior, full gate, regeneration hashes, shell syntax/invalid mode, read-only Bazel query, clean diff and absence of source/dependency churn. Planning reviewer /root/planning_review passed rounds 1 and 2 (Findings: none), with round 2 confirming workflow delivery ownership and subsequent exact-head merge authority. Exact-head GitHub CI, assignment and merge remain post-closeout delivery gates.

## Delivery checks

No open PRs at entry. Repository permits all merge methods and allow_auto_merge=true; viewer ADMIN. main branch protection returns 404 and branch rules/rulesets are empty. User subsequently authorizes merging with --auto --merge --match-head-commit only after every actual CI check on exact head passes and no blocking reviews remain. Merge commit preserves plan/implementation/closeout audit history; no protection changes or bypass.
