package io.github.haobv.solvetowatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * One question with four answer choices, from the Vietnamese primary curriculum (docs/curriculum.md).
 * To add a grade: write a gradeN method, list N in GRADES and add it to next(); MathQuestionTest then
 * checks every question it can produce.
 */
final class MathQuestion {
    /** Grades next() has questions for. */
    static final int[] GRADES = {3};

    private static final int[] NEAR = {1, -1, 2, -2, 10, -10, 9, -9, 11, -11, 100, -100};

    final String text;
    final int answer;
    final List<Integer> choices;

    private MathQuestion(String text, int answer, Random r) {
        this.text = text;
        this.answer = answer;
        this.choices = choices(answer, r);
    }

    static MathQuestion next(int grade, Random r) {
        switch (grade) {
            case 3:
            default:
                return grade3(r);
        }
    }

    /** Lớp 3: +/− within 1000, ×/: tables 2–9, 2-digit × 1-digit, 3-digit : 1-digit, two-step expressions. */
    private static MathQuestion grade3(Random r) {
        switch (r.nextInt(7)) {
            case 0: { // cộng có nhớ trong phạm vi 1000
                int a = between(r, 100, 899), b = between(r, 10, 999 - a);
                return new MathQuestion(a + " + " + b, a + b, r);
            }
            case 1: { // trừ trong phạm vi 1000
                int a = between(r, 200, 999), b = between(r, 10, a - 10);
                return new MathQuestion(a + " − " + b, a - b, r);
            }
            case 2: { // bảng nhân 2–9
                int a = between(r, 2, 9), b = between(r, 2, 9);
                return new MathQuestion(a + " × " + b, a * b, r);
            }
            case 3: { // bảng chia 2–9
                int b = between(r, 2, 9), q = between(r, 2, 9);
                return new MathQuestion(b * q + " : " + b, q, r);
            }
            case 4: { // số có 2 chữ số nhân số có 1 chữ số
                int a = between(r, 11, 99), b = between(r, 2, 9);
                return new MathQuestion(a + " × " + b, a * b, r);
            }
            case 5: { // số có 3 chữ số chia hết cho số có 1 chữ số
                int b = between(r, 2, 9), q = between(r, (100 + b - 1) / b, 999 / b);
                return new MathQuestion(b * q + " : " + b, q, r);
            }
            default: { // biểu thức hai phép tính
                switch (r.nextInt(3)) {
                    case 0: {
                        int a = between(r, 10, 99), b = between(r, 2, 9), c = between(r, 2, 9);
                        return new MathQuestion(a + " + " + b + " × " + c, a + b * c, r);
                    }
                    case 1: {
                        int c = between(r, 2, 9), q = between(r, 2, 9), b = between(r, 1, 50);
                        return new MathQuestion("(" + (c * q + b) + " − " + b + ") : " + c, q, r);
                    }
                    default: {
                        int a = between(r, 2, 9), b = between(r, 2, 9), c = between(r, 1, a * b - 1);
                        return new MathQuestion(a + " × " + b + " − " + c, a * b - c, r);
                    }
                }
            }
        }
    }

    /** Wrong choices close to the answer, so guessing by size does not work. */
    private static List<Integer> choices(int answer, Random r) {
        List<Integer> deltas = new ArrayList<>();
        for (int d : NEAR) deltas.add(d);
        Collections.shuffle(deltas, r);
        Set<Integer> set = new LinkedHashSet<>();
        set.add(answer);
        for (int d : deltas) {
            if (set.size() == 4) break;
            if (Math.abs(d) == 100 && answer < 100) continue;
            if (answer + d >= 0) set.add(answer + d);
        }
        List<Integer> list = new ArrayList<>(set);
        Collections.shuffle(list, r);
        return list;
    }

    private static int between(Random r, int min, int max) {
        return min + r.nextInt(max - min + 1);
    }
}
