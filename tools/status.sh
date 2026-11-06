#!/usr/bin/env bash
# What the app is doing on the TV: service bound, auto-start, settings (PIN hidden), app in front.
# Usage: TV_HOST=192.168.1.50 tools/status.sh
set -euo pipefail
cd "$(dirname "$0")/.."
. tools/lib.sh
connect
echo "version:    $(tv shell dumpsys package $PKG | grep -m1 versionName | tr -d ' \r')"
if bound; then echo "service:    bound"; else echo "service:    NOT bound (tools/install.sh, or docs/devices.md)"; fi
echo "auto-start: $(tv shell appops get $PKG AUTO_START 2>/dev/null | tr -d '\r' | cut -d';' -f1)"
echo "settings:"
tv shell run-as $PKG cat shared_prefs/gate.xml 2>/dev/null | grep -v '"pin"' | grep '<int\|<boolean\|<long' | sed 's/^ */  /' || echo "  (defaults from config.xml)"
echo "in front:   $(tv shell dumpsys activity activities | grep -m1 mFocusedApp | sed 's/.*u0 //; s/ t[0-9]*}//')"
