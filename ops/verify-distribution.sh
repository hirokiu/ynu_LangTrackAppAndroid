#!/usr/bin/env bash
set -Eeuo pipefail
apk=${1:?Usage: verify-distribution.sh APK EXPECTED_SHA256}
expected=${2:?Expected certificate SHA256 required}
: "${ANDROID_SDK_ROOT:?Set ANDROID_SDK_ROOT}"
build_tools=${KIROKUN_BUILD_TOOLS:-36.0.0}
certs=$("$ANDROID_SDK_ROOT/build-tools/$build_tools/apksigner" verify --print-certs "$apk")
if [[ "$certs" == *'CN=Android Debug'* ]]; then
  echo 'Rejected: Android Debug signature' >&2; exit 1
fi
actual=$(printf '%s\n' "$certs" | sed -n 's/^Signer #1 certificate SHA-256 digest: //p')
[[ -n "$actual" && "$actual" == "$expected" ]] || { echo 'Rejected: certificate mismatch' >&2; exit 1; }
metadata=$("$ANDROID_SDK_ROOT/build-tools/$build_tools/aapt" dump badging "$apk")
[[ "$metadata" != *application-debuggable* ]] || { echo 'Rejected: debuggable APK' >&2; exit 1; }
[[ "$metadata" == *"package: name='com.alchembright.dev.langtrackapp'"* ]] || { echo 'Rejected: wrong application ID' >&2; exit 1; }
echo 'PASS: Proto package, non-debuggable APK, expected non-debug signing certificate.'
shasum -a 256 "$apk"
