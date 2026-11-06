package io.github.haobv.solvetowatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Checks every grade's questions by working them out again from the text a child sees. */
public class MathQuestionTest {
    private static final int SAMPLES = 20000;

    @Test
    public void everyQuestionHasTheRightAnswerAmongFourChoices() {
        for (int grade : MathQuestion.GRADES) {
            Random r = new Random(grade);
            for (int i = 0; i < SAMPLES; i++) {
                MathQuestion q = MathQuestion.next(grade, r);
                String where = "grade " + grade + ": " + q.text;
                assertEquals(where, Expr.eval(q.text), q.answer);
                assertTrue(where, q.answer >= 0);
                assertEquals(where, 4, q.choices.size());
                assertEquals(where + " repeats a choice", 4, new HashSet<>(q.choices).size());
                assertTrue(where, q.choices.contains(q.answer));
                for (int c : q.choices) assertTrue(where + " has a negative choice", c >= 0);
            }
        }
    }

    @Test
    public void grade3StaysWithin1000() {
        Random r = new Random(3);
        for (int i = 0; i < SAMPLES; i++) {
            MathQuestion q = MathQuestion.next(3, r);
            assertTrue(q.text, q.answer <= 1000);
            for (String n : q.text.split("[^0-9]+")) {
                if (!n.isEmpty()) assertTrue(q.text, Integer.parseInt(n) <= 1000);
            }
        }
    }

    @Test
    public void configXmlIsValid() throws Exception {
        String xml = new String(Files.readAllBytes(Paths.get("src/main/res/values/config.xml")), StandardCharsets.UTF_8);
        int grade = integer(xml, "grade");
        assertTrue("config.xml grade " + grade + " is not in MathQuestion.GRADES", contains(MathQuestion.GRADES, grade));
        int minutes = integer(xml, "default_minutes");
        assertTrue("config.xml default_minutes " + minutes + " is not in Prefs.MINUTE_CHOICES", contains(Prefs.MINUTE_CHOICES, minutes));
        int streak = integer(xml, "default_streak");
        assertTrue("config.xml default_streak must be 1 to 5", streak >= 1 && streak <= 5);
        assertTrue("config.xml needs at least one gated package", xml.matches("(?s).*<item>[a-zA-Z0-9_.]+</item>.*"));
    }

    private static int integer(String xml, String name) {
        Matcher m = Pattern.compile("<integer name=\"" + name + "\">(\\d+)</integer>").matcher(xml);
        assertTrue("config.xml has no integer " + name, m.find());
        return Integer.parseInt(m.group(1));
    }

    private static boolean contains(int[] values, int value) {
        for (int v : values) if (v == value) return true;
        return false;
    }

    /** Evaluates the question text: + − × : and brackets, with × and : before + and −. Division must be exact. */
    static final class Expr {
        private final String s;
        private int pos;

        private Expr(String s) {
            this.s = s.replace(" ", "");
        }

        static int eval(String text) {
            Expr e = new Expr(text);
            int v = e.sum();
            assertEquals("unexpected text in " + text, e.s.length(), e.pos);
            return v;
        }

        private int sum() {
            int v = product();
            while (pos < s.length() && (s.charAt(pos) == '+' || s.charAt(pos) == '−')) {
                char op = s.charAt(pos++);
                int w = product();
                v = op == '+' ? v + w : v - w;
            }
            return v;
        }

        private int product() {
            int v = atom();
            while (pos < s.length() && (s.charAt(pos) == '×' || s.charAt(pos) == ':')) {
                char op = s.charAt(pos++);
                int w = atom();
                if (op == ':') {
                    assertEquals("division with a remainder in " + s, 0, v % w);
                    v /= w;
                } else {
                    v *= w;
                }
            }
            return v;
        }

        private int atom() {
            if (s.charAt(pos) == '(') {
                pos++;
                int v = sum();
                assertEquals(')', s.charAt(pos++));
                return v;
            }
            int start = pos;
            while (pos < s.length() && Character.isDigit(s.charAt(pos))) pos++;
            return Integer.parseInt(s.substring(start, pos));
        }
    }
}
