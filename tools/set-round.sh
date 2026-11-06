#!/usr/bin/env bash
# Set the minutes per round on the TV and start a fresh round, e.g. 1 for testing, then back to 15.
# The parent can do the same on the TV in GuGu Toán's settings. Works on the debug build (run-as).
# Usage: TV_HOST=192.168.1.50 tools/set-round.sh 1
set -euo pipefail
cd "$(dirname "$0")/.."
. tools/lib.sh
M="${1:?usage: set-round.sh <minutes: 1 5 10 15 20 30 45 60>}"
case " 1 5 10 15 20 30 45 60 " in *" $M "*) ;; *) echo "minutes must be one of 1 5 10 15 20 30 45 60" >&2; exit 1 ;; esac
connect
# Stop the app first: a running process would write its cached copy back over this edit.
tv shell am force-stop $PKG
tv shell run-as $PKG sh <<SH
f=shared_prefs/gate.xml
mkdir -p shared_prefs
[ -f \$f ] || printf '<?xml version="1.0" encoding="utf-8" standalone="yes" ?>\n<map>\n</map>\n' > \$f
sed -i -e '/name="minutes"/d' -e '/name="watched"/d' -e '/name="locked"/d' \$f
sed -i 's|</map>|    <int name="minutes" value="$M" />\n</map>|' \$f
SH
# force-stop unbinds the accessibility service: bind it again.
enable_service
echo "Round set to $M minute(s); the count starts again from 0."
