# AGENTS.md

Instructions for coding agents (Claude Code, Codex, Gemini CLI, Cursor and others). A parent has asked you to
"clone this repo and follow it": you are going to set up **Solve to Watch** (shown on the TV as *GuGu Toán*)
on their Android TV, personalised for their child. People start at [README.md](README.md).

## What you are building

A small Android TV app. While a gated app (YouTube by default) is on screen, it counts the minutes; when a round
is used up it pauses the video and shows a full-screen math quiz. The child must answer a number of questions
right **in a row**; then the video plays on and a new round starts. Parents unlock, snooze or change settings
with a PIN. The full behaviour is in [docs/spec.md](docs/spec.md).

The code in `app/` is a working reference, tested on a real TV. **Personalise it; do not rewrite it.** Add code
only for what the parent asks that the reference does not do, and follow the spec when you do.

## Which job are you doing?

| You were asked to | Do this |
|---|---|
| Set the app up on a family's TV ("clone and follow", "install it on my TV") | Follow [The setup, step by step](#the-setup-step-by-step) on the real TV. |
| Change something on a TV where it is already installed (another grade, more minutes, a new character) | Make the change as in [docs/customize.md](docs/customize.md), then steps 5 and 6. |
| Remove it | `TV_HOST=<ip> tools/uninstall.sh`, then step 7. |
| Change this repo itself (a fix, a new grade for everyone, a device note) | [Changing this repo](#changing-this-repo). |

## The setup, step by step

Talk to the parent in their language (the README is Vietnamese; answer in Vietnamese unless they write
otherwise). Tell them what you are about to do before each step that changes their computer or TV.

### 1. Check the computer

The build needs **JDK 17**, **Android SDK platform 34 with build-tools 34.0.0**, and **adb**. Check what is
there; for anything missing, say what you will install and how big it is (about 1–1.5 GB in all), and wait for
the parent's yes. On macOS with Homebrew:

```
brew install --cask android-platform-tools
brew install openjdk@17
brew install --cask android-commandlinetools
sdkmanager --sdk_root=/opt/homebrew/share/android-commandlinetools "platforms;android-34" "build-tools;34.0.0"
```

(`sdkmanager` needs `JAVA_HOME` pointing at the JDK 17 and the licences accepted with `--licenses`.) On Linux or
Windows use the platform's packages or Android Studio's SDK manager. `tools/lib.sh` finds the usual locations.

### 2. Ask the parent, in one message

Give a short checklist with the defaults filled in, so "all defaults" is a valid answer:

| Question | Default | Goes to |
|---|---|---|
| TV model, or let you find the TV on the network | find it | `TV_HOST` |
| The child's school grade (1–5) | 3 | `config.xml` `grade` |
| What the character calls the child ("GuGu đố **Bin** nè!") | `bé` | `strings.xml` `child_name` |
| Which apps to gate | YouTube | `config.xml` `gated_packages` |
| Minutes per round, right answers in a row | 15, 3 | `config.xml` |
| Character: GuGu, a picture they give you, or none | GuGu | `res/drawable-nodpi/` |
| Language on screen | Vietnamese | `strings.xml` |

Do **not** ask for or store their PIN: the app starts with `0000` and you will ask them to change it on the TV
in step 6. A child's name or photo stays on this computer (see the rules).

### 3. Connect to the TV

- The TV needs Developer options with **USB debugging** on (README, "Cần chuẩn bị"). Many TVs then open
  network ADB on port 5555. Find the TV's IP (ask the parent: Settings → Network → the Wi-Fi network shows it,
  or look in `arp -a`); don't scan beyond the home network.
- `adb connect <ip>:5555`. Tell the parent **before** you connect that the TV will show *"Allow USB
  debugging?"* and that they should tick *Always allow* and press OK.
- Port 5555 closed: the TV may only offer *Wireless debugging* with a pairing code (`adb pair`), see
  [docs/devices.md](docs/devices.md).
- Read `getprop ro.product.model`, `ro.build.version.sdk` (must be **26 or higher**) and
  `pm list packages -3`. Stop and say so if this is not Android TV / Google TV or the SDK is too old.
- For the gated-apps question, show the parent the TV's real apps (`pm list packages` plus their labels) rather
  than package names they have to guess. YouTube on TV is `com.google.android.youtube.tv`.
- Look up the TV's brand in [docs/devices.md](docs/devices.md) now: some firmware needs an extra permission.

### 4. Personalise

Everything a family changes is listed in [docs/customize.md](docs/customize.md). Usually that is only
`app/src/main/res/values/config.xml` and two strings. A grade other than those in `MathQuestion.GRADES` means
writing its questions from [docs/curriculum.md](docs/curriculum.md); a new character means three pictures.

### 5. Test, build and install

```
TV_HOST=<ip> tools/install.sh
```

It runs the unit tests (`MathQuestionTest` works out every question again from its text and checks
`config.xml`), builds the debug APK, installs it, grants the overlay and vendor auto-start permissions and
switches the accessibility service on **without dropping other services the TV already has on**. Use this
script; do not install by hand, and do not write your own `enabled_accessibility_services` value. If a test
fails after your change, fix the change.

### 6. Verify on the real TV, with screenshots

Look at every screen you claim works: `adb -s <ip>:5555 exec-out screencap -p > shots/name.png`, then open it.
Video apps can give black screenshots; then check `dumpsys window | grep mCurrentFocus` instead.

1. `TV_HOST=<ip> tools/set-round.sh 1` for a one-minute round. Open a gated app
   (`monkey -p <package> -c android.intent.category.LEANBACK_LAUNCHER 1`), wait about 70 seconds: the quiz must be
   in front and the video paused.
2. Press Back (`input keyevent BACK`): the quiz must stay. Press Home, open the gated app again: the quiz must come
   straight back.
3. Answer wrong once (look at the screenshot to pick), then right enough times in a row: the success screen,
   then the video again. **Ask the parent to try it with the real remote**; your injected keys are not proof.
4. **Reboot the TV** (`adb shell sync`, wait 15 s, `adb reboot`) and run `TV_HOST=<ip> tools/status.sh`: the
   service must be *bound*. If not, the firmware blocks auto-start: [docs/devices.md](docs/devices.md).
5. `TV_HOST=<ip> tools/set-round.sh <their minutes>` and show `tools/status.sh`.
6. Ask the parent to open GuGu Toán on the TV, enter `0000` and **change the PIN** (last row).

### 7. Finish

- Give the parent a short summary in their language: what was set, how to reach the settings (the app in the
  TV's app list, or the "Bố mẹ" button), how to snooze, how to remove it.
- **Close ADB**: ask the parent to switch USB debugging (and Network/Wireless debugging) off in Developer
  options. Port 5555 is plaintext; anyone on the network could otherwise get a shell on the TV. Afterwards
  `adb connect <ip>:5555` must be refused. Later changes need debugging switched on again.

## Rules

- **No network.** The app has no `INTERNET` permission, no analytics, no ads, no accounts. Do not add any.
- **Read, don't block.** The accessibility service only reads which app is in front. It does not filter keys
  (that would break holding the volume button on some TVs) and does not read what is on screen.
- **Leave the TV alone.** Never uninstall or disable the TV's apps, never root, never change settings the app
  does not need. Keep other accessibility services enabled (`tools/lib.sh` does).
- **The child's data stays home.** Names, photos and the PIN are never committed, pushed or pasted anywhere
  public. Personal pictures go in `family/` (git-ignored) before you copy them into the app; if you commit the
  personalised app, keep it on a local branch and don't push it unless the parent asks, to a private remote.
- **Always test on the TV** with a one-minute round before setting the real one; never claim a step works
  from the code alone.
- **Ask before** installing software on the computer, rebooting the TV, or anything the parent would notice.
- **Stop and say so** when the TV refuses (no ADB, SDK below 26, install blocked): don't loop on retries.

## Repo map

| Path | What it is |
|---|---|
| `app/src/main/res/values/config.xml` | Gated apps, round minutes, streak, grade, first-run PIN. Start here. |
| `app/src/main/res/values/strings.xml` | Every word on screen, the character's and the child's names. |
| `app/src/main/res/drawable-nodpi/` | Character pictures (`gugu_teacher` asks, `gugu_happy` celebrates, `gugu` on a wrong answer and in settings), background, icon. |
| `app/src/main/java/.../GateService.java` | Accessibility service: counts time in gated apps, pauses, shows the quiz. |
| `app/src/main/java/.../QuizActivity.java` | The quiz screen. |
| `app/src/main/java/.../MathQuestion.java` | Question generator per grade, with near-miss wrong choices. |
| `app/src/main/java/.../Prefs.java` | All state: settings, the current round, today's stats. |
| `app/src/main/java/.../SettingsActivity.java`, `PinActivity.java` | Parent screens. |
| `app/src/test/.../MathQuestionTest.java` | JVM tests: answers, choices, ranges, `config.xml`. `./gradlew testDebugUnitTest`. |
| `tools/` | `install.sh`, `set-round.sh`, `status.sh`, `uninstall.sh` (all take `TV_HOST`). |
| `docs/` | `spec.md` behaviour, `curriculum.md` grades 1–5, `customize.md` recipes, `devices.md` TV quirks. |
| `family/` | This family's own files; git-ignored. |

## Changing this repo

- One topic per pull request: a new grade, a device note, a fix.
- A new grade: add it to `MathQuestion` and `GRADES`; the tests then check all of its questions. Say which
  textbook or curriculum you followed.
- A device note: add what you saw (model, Android version, the log line, the fix) to `docs/devices.md`. No IPs,
  serial numbers, account names or Wi-Fi names in anything you commit.
- Keep the app small and plain: Java, no extra libraries, `minSdk` 26, no new permissions without an issue first.
- `./gradlew testDebugUnitTest assembleDebug` must pass, and a change to what the child sees needs a screenshot
  from a real TV in the pull request.
