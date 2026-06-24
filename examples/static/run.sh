#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
CONFIG="$(mktemp "${TMPDIR:-/tmp}/ariake-static.XXXXXX.properties")"

trap 'rm -f "${CONFIG}"' EXIT

cat "${SCRIPT_DIR}/application.properties" > "${CONFIG}"
printf 'static.example.staticRoot=%s/content\n' "${SCRIPT_DIR}" >> "${CONFIG}"

cd "${REPO_ROOT}"
bazel run //examples/static:server -- --config="${CONFIG}" "$@"
