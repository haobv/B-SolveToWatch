# Math by grade (Vietnamese primary curriculum)

A summary of what each grade practises in the Vietnamese 2018 general-education programme (Chương trình GDPT 2018),
to write question generators from. Textbook series (Kết nối tri thức, Chân trời sáng tạo, Cánh diều) order the
topics differently within a year: when the parent says where the child is now ("đang học bảng nhân 6"), follow
the parent. Questions must have a whole-number answer of 0 or more unless you also change how answers are shown.

| Grade | Numbers | Questions to generate | Notes |
|---|---|---|---|
| **1** | to 100 | `+`/`−` within 10, then within 20 without carrying; 2-digit ± 1-digit and 2-digit ± 2-digit within 100 **without** carrying | Small numbers, large type. Comparing (`>`, `<`, `=`) would need symbol choices instead of numbers. |
| **2** | to 1000 | `+`/`−` **with** carrying within 100; `+`/`−` within 1000 without carrying; × and : tables of **2 and 5** | Keep products and quotients from the tables only. |
| **3** | to 10 000 (first half: 1000) | `+`/`−` with carrying within 1000; × and : tables **2–9**; 2- and 3-digit × 1-digit; 2- and 3-digit : 1-digit (exact); two-step expressions, brackets first | **Implemented** (first-half range, within 1000). Second half: the same within 10 000. |
| **4** | many digits | `+`/`−` of multi-digit numbers; × by 2-digit numbers; : by 2-digit numbers (exact); expressions | Fractions are also grade 4, but need a non-integer answer type. |
| **5** | decimals | `+`/`−`/`×`/`:` of decimals; percentages | Needs answers shown as decimals (`3,5`, Vietnamese decimal comma): change `MathQuestion.answer` to a string or a fixed-point value and update the test's evaluator. |

## Writing a grade

1. Add `gradeN(Random r)` to `MathQuestion`, mixing the question types of that row; keep every answer ≥ 0 and
   every division exact.
2. Add `N` to `GRADES` and a `case N:` to `next()`.
3. Add a range test like `grade3StaysWithin1000` for the grade's number range.
4. `./gradlew testDebugUnitTest`: the test works out each question again from its text, so a wrong answer, a
   symbol it cannot read, or a division with a remainder fails it.

Symbols on screen: `+`, `−` (U+2212, not a hyphen), `×`, `:` for division as Vietnamese schools write it, brackets.
