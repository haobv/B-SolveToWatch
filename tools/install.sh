#!/usr/bin/env bash
# Test, build and install the app on the TV, grant what it needs and switch its service on.
# Usage: TV_HOST=192.168.1.50 tools/install.sh
set -euo pipefail
cd "$(dirname "$0")/.."
. tools/lib.sh
build_env
./gradlew testDebugUnitTest assembleDebug -q --console=plain
connect
SDK=$(tv shell getprop ro.build.version.sdk | tr -d '\r')
[ "$SDK" -ge 26 ] || { echo "This TV runs Android SDK $SDK; the app needs 26 (Android 8) or newer." >&2; exit 1; }
# Push and pm install rather than adb install, which can wait for an on-screen confirmation on Android 14.
tv push app/build/outputs/apk/debug/app-debug.apk /data/local/tmp/solve-to-watch.apk >/dev/null
tv shell pm install -r /data/local/tmp/solve-to-watch.apk
tv shell rm /data/local/tmp/solve-to-watch.apk
grant
enable_service
echo "Installed and running. Next: TV_HOST=$TV_HOST tools/set-round.sh 1 for a one-minute test round."
