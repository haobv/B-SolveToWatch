# TV notes

What specific TVs need. Add your TV when you hit something new: model, Android version, the log line, the fix.
No IP addresses, serial numbers or account names.

## All TVs

- **Service not bound after install:** toggling accessibility binds it. `tools/lib.sh` sets the services to
  empty, `accessibility_enabled` 0, waits, then writes the list back and sets 1. `tools/status.sh` shows the state.
- **Force stop unbinds the service** until accessibility is toggled again or the TV reboots
  (`tools/set-round.sh` re-enables it for that reason).
- **`adb install` hangs on Android 14:** it waits for a confirmation on screen; `tools/install.sh` pushes the
  APK and runs `pm install` instead.
- **Only Wireless debugging, no port 5555:** pair with the code from Developer options → Wireless debugging →
  Pair device (`adb pair <ip>:<port>`), `adb connect`, then `adb tcpip 5555` so the tools can use 5555.

## TCL Google TV

Tested: TCL "Smart TV Pro", board G15 (Realtek), Google TV 14 (SDK 34), firmware UTT2.250416.001 AU12.

- Switching USB debugging on opens network ADB on port 5555 directly.
- **After a reboot the service does not start.** Log:
  `TclAppBoot: ... calleePkgName = 'io.github.haobv.solvetowatch' ... bootType = 'bind_service' ... isAllow = 'false',
  reason = 'callee_does't_have_OP_AUTO_START_permission.'`, and `accessibility_enabled` reads 0 again.
  Fix: `appops set <package> AUTO_START allow` then `appops write-settings` (`tools/lib.sh` → `grant`), and enable
  the service again. Check `appops get <package> AUTO_START` says `allow`: run through `cmd appops` inside a longer
  shell line, the same command once left it at `ignore`.
- Reboot and check with `tools/status.sh` before closing ADB.

## Other brands

Not tested yet. Xiaomi (PatchWall), Hisense and others have their own auto-start or "app management" switches;
if the service is not bound after a reboot, look in `logcat` for the package name and a "forbid" or "restrict" line.
