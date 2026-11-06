# Customising for a family

Each recipe says what to change. After any change: `TV_HOST=<ip> tools/install.sh`, then check it on the TV
(AGENTS.md, step 6).

## Gated apps

`config.xml` → `gated_packages`, one `<item>` per package. Find package names on the TV with
`pm list packages -3` (installed apps) and `pm list packages | grep -i <name>`. Common ones:

| App | Package |
|---|---|
| YouTube | `com.google.android.youtube.tv` |
| YouTube Kids | `com.google.android.youtube.tvkids` |
| Netflix | `com.netflix.ninja` |
| FPT Play | `net.fptplay.ottbox` |
| VTV Go | `vn.vtv.vtvgotv` |

Check on the TV: the name in the quiz's top pill ("YouTube đang tạm dừng") comes from the paused app.

## Minutes, streak, PIN

`config.xml` → `default_minutes` (one of 1, 5, 10, 15, 20, 30, 45, 60), `default_streak` (1–5). These are
first-run defaults only: on an installed TV use `tools/set-round.sh <minutes>` or the settings screen. Never put
the parent's real PIN in `default_pin`; they change it on the TV.

## Grade

`config.xml` → `grade`. Grades already written are in `MathQuestion.GRADES`; others: [curriculum.md](curriculum.md).

## Names and language

`strings.xml`:
- `character_name` (GuGu) and `child_name` (bé) fill the title "%1$s đố %2$s nè!".
- `app_name` is the tile's name in the TV's app list.
- For another language, translate every string in the file (keep the `%s` / `%d` placeholders). For both languages
  following the TV's language, keep Vietnamese in `values/` and add `values-en/strings.xml`.

## Character

Three pictures in `app/src/main/res/drawable-nodpi/`, PNG with a transparent background, about 480 px tall:

| File | Shown when |
|---|---|
| `gugu_teacher.png` | asking a question |
| `gugu_happy.png` | the round is unlocked |
| `gugu.png` | after a wrong answer, and in the settings header |

From one picture the parent sends: put it in `family/`, remove the background if it has one, crop to the figure,
and use it for all three (or ask whether they have happy/neutral versions). With no character, set the
`ImageView` in `activity_quiz.xml` to `gone` and move the text column's `layout_marginStart` from 210dp to 24dp.
Also replace `icon.png` and `drawable-xhdpi/banner.png` (320×180, the tile in the TV's app list).
GuGu's pictures are not under the MIT licence ([ASSETS-LICENSE.md](../ASSETS-LICENSE.md)); the family's own are theirs.

## Colours and look

Light "glass" style: background `drawable-nodpi/bg.jpg` (1920×1080, soft colour field at the top), white card
(`drawable/card.xml`), focus colour `#0A84FF` in `drawable/answer.xml`, `row.xml`, `pill_glass_focus.xml`, text
colours in `color/`. Keep focus obvious from across the room: one strong colour on the focused button, nothing else.
