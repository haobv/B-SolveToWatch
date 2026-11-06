# Behaviour

What the app does, independent of how `app/` implements it. Change the code, keep this true; change this
only when the parent wants different behaviour, and say so in the summary.

## Rounds

- A **round** is `minutes` (default 15) of time with a **gated app** in front: the top-most application window
  belongs to a package in `gated_packages`. Overlays (volume bar, input menu) do not stop the count.
- Time counts only while the screen is on and the gate is active (switched on, not snoozed). It adds up across
  visits: leaving the app and coming back does not reset it.
- When a round is used up the round becomes **locked**: the video is paused (media pause key) and the quiz opens
  in front of it.
- While locked, any gated app coming to the front brings the quiz back at once. Home leaves the quiz; Back does not.

## The quiz

- One question at a time, from the configured grade, with **four choices** picked with the remote's arrows and OK.
  Wrong choices are close to the answer (±1, ±2, ±9, ±10, ±11, ±100), never negative, never repeated.
- Progress dots, one per answer needed (`streak`, default 3). A right answer fills a dot; a wrong one empties all
  dots, shows the right answer ("Chưa đúng rồi, 61 × 9 = 549. Làm lại nhé!") and moves to a new question.
- `streak` right answers in a row unlock: a short celebration (about 2 s), the quiz closes, the video plays and a
  new round starts from 0.
- The character changes with the moment: asking (`gugu_teacher`), wrong answer (`gugu`), success (`gugu_happy`).
- A small **Bố mẹ** button opens the PIN pad. The right PIN unlocks the round and opens the settings.

## Parent settings

Reached from the app's tile in the TV's app list (PIN first) or from the quiz.

| Row | Values |
|---|---|
| On / off | Off also unlocks a locked round |
| Minutes per round | 1, 5, 10, 15, 20, 30, 45, 60 |
| Right answers in a row | 1 to 5 |
| Snooze | off → 1 hour → 3 hours → until midnight; snoozing also unlocks |
| Change PIN | four digits, entered on the on-screen pad or the remote's number keys |

Under the rows: today's minutes watched, quizzes passed, answers right of answers given (resets at midnight).

## Privacy and limits

- No internet permission. Nothing leaves the TV. The service reads only which package is in front.
- It is a fence for young children, not a lock: Force stop in the TV's settings ends it until the next reboot.
