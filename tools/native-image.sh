#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

if ! command -v native-image >/dev/null 2>&1; then
  echo "native-image is not installed. Install GraalVM Native Image to build Ariake native binaries." >&2
  exit 2
fi

cd "${ROOT}"
bazel build //examples/hello:server_deploy.jar
native-image \
  -jar bazel-bin/examples/hello/server_deploy.jar \
  ariake-hello
