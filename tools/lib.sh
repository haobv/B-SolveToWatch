# Shared by the scripts in tools/. Source it, don't run it.
PKG=io.github.haobv.solvetowatch
SVC=$PKG/$PKG.GateService
: "${TV_HOST:?set TV_HOST to the IP address of the TV, e.g. TV_HOST=192.168.1.50 $0}"
SERIAL="$TV_HOST:${TV_PORT:-5555}"

tv() { adb -s "$SERIAL" "$@"; }

# Connect over network ADB. The first time, the TV asks "Allow USB debugging?": the parent accepts it.
connect() {
  command -v adb >/dev/null || { echo "adb not found: install Android platform-tools first" >&2; exit 1; }
  adb connect "$SERIAL" >/dev/null 2>&1 || true
  for _ in $(seq 1 30); do
    [ "$(tv get-state 2>/dev/null)" = device ] && return 0
    [ "${_}" = 2 ] && echo "Waiting for the TV: accept \"Allow USB debugging?\" on the screen (tick Always allow)."
    sleep 3
    adb connect "$SERIAL" >/dev/null 2>&1 || true
  done
  echo "Cannot reach $SERIAL. Is the TV on, on the same network, with USB debugging on?" >&2
  exit 1
}

# JDK 17 and the Android SDK, found where Android Studio or Homebrew put them.
build_env() {
  if [ -z "${JAVA_HOME:-}" ]; then
    for jh in "$(/usr/libexec/java_home -v 17 2>/dev/null || true)" /opt/homebrew/opt/openjdk@17 /usr/local/opt/openjdk@17 /usr/lib/jvm/java-17-openjdk-amd64; do
      [ -n "$jh" ] && [ -x "$jh/bin/java" ] && case "$("$jh/bin/java" -version 2>&1)" in *'version "17.'*) JAVA_HOME=$jh; break ;; esac
    done
  fi
  : "${JAVA_HOME:?no JDK 17 found: install one (brew install openjdk@17) or set JAVA_HOME}"
  export JAVA_HOME
  if [ ! -f local.properties ]; then
    for sdk in "${ANDROID_HOME:-}" ~/Library/Android/sdk ~/Android/Sdk /opt/homebrew/share/android-commandlinetools /usr/local/share/android-commandlinetools; do
      [ -n "$sdk" ] && [ -d "$sdk/platforms" ] && { echo "sdk.dir=$sdk" > local.properties; break; }
    done
  fi
  [ -f local.properties ] || { echo "no Android SDK found: install platform 34 and build-tools 34, or set ANDROID_HOME" >&2; exit 1; }
}

# Overlay permission (the quiz may start while another app is in front) and the vendor auto-start
# switches: TCL's TclAppBoot refuses to bind the service after a reboot without AUTO_START (docs/devices.md).
grant() {
  tv shell appops set $PKG SYSTEM_ALERT_WINDOW allow
  tv shell appops set $PKG AUTO_START allow 2>/dev/null || true
  tv shell appops set $PKG APP_AUTO_START allow 2>/dev/null || true
  tv shell appops write-settings 2>/dev/null || true
}

# Add the service to enabled_accessibility_services, keeping every other service already there, and
# toggle accessibility so Android binds it now. Fails unless the service is bound afterwards.
enable_service() {
  local cur new
  cur=$(tv shell settings get secure enabled_accessibility_services | tr -d '\r')
  new=$(printf '%s' "$cur" | tr ':' '\n' | grep -v -e "^$SVC\$" -e '^null$' -e '^$' | { cat; echo "$SVC"; } | paste -sd: -)
  tv shell "settings put secure enabled_accessibility_services ''; settings put secure accessibility_enabled 0; sleep 1; settings put secure enabled_accessibility_services $new; settings put secure accessibility_enabled 1"
  for _ in 1 2 3 4 5; do
    sleep 2
    bound && return 0
  done
  echo "The service did not start. Check: tv shell logcat -d | grep -i $PKG (docs/devices.md)" >&2
  return 1
}

bound() { tv shell dumpsys accessibility | grep 'Bound services' | grep -q 'GuGu\|solvetowatch\|giải toán'; }
