#!/usr/bin/env bash
# Remove the app from the TV, leaving every other accessibility service as it was.
# Usage: TV_HOST=192.168.1.50 tools/uninstall.sh
set -euo pipefail
cd "$(dirname "$0")/.."
. tools/lib.sh
connect
cur=$(tv shell settings get secure enabled_accessibility_services | tr -d '\r')
rest=$(printf '%s' "$cur" | tr ':' '\n' | grep -v -e "^$SVC\$" -e '^null$' -e '^$' | paste -sd: - || true)
tv shell settings put secure enabled_accessibility_services "${rest:-}"
[ -n "$rest" ] || tv shell settings put secure accessibility_enabled 0
tv shell pm uninstall $PKG
echo "Removed."
