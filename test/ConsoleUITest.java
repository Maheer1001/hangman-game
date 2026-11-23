import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.junit.jupiter.api.Test;

class ConsoleUITest {

    @Test
    void difficultyIsCaseInsensitiveAndRepromptsOnInvalid() {
        try (StdoutCapture cap = new StdoutCapture().withInput("extreme", "", "MeDiUm")) {
            ConsoleUI ui = new ConsoleUI(); // reads System.in, so build it after swapping input
            assertEquals("medium", ui.askDifficulty());
            assertEquals(2, count(cap.output(), "Invalid. Please choose easy, medium, or hard"));
        }
    }

    @Test
    void letterRejectsEmptyMultiCharNonLetterAndAlreadyGuessed() {
        try (StdoutCapture cap = new StdoutCapture().withInput("", "ab", "7", "?", "e", "Q")) {
            ConsoleUI ui = new ConsoleUI();
            assertEquals('q', ui.askLetter(Set.of('e')), "uppercase is accepted and lowercased");
            assertEquals(5, count(cap.output(), "Invalid or You have already guessed this!"));
        }
    }

    @Test
    void replayOnlyOnYes() {
        try (StdoutCapture ignored = new StdoutCapture().withInput("YES", "yes", "no", "y")) {
            ConsoleUI ui = new ConsoleUI();
            assertTrue(ui.askReplay());
            assertTrue(ui.askReplay());
            assertFalse(ui.askReplay());
            assertFalse(ui.askReplay(), "anything other than yes means no");
        }
    }

    @Test
    void showsStateAndResult() {
        try (StdoutCapture cap = new StdoutCapture()) {
            ConsoleUI ui = new ConsoleUI();
            ui.showGameState("c_t", 7);
            ui.showResult(true, "cat");
            ui.showResult(false, "dog");
            String out = cap.output();
            assertTrue(out.contains("Your word is: c_t"));
            assertTrue(out.contains("Attempts left: 7"));
            assertTrue(out.contains("You won!"));
            assertTrue(out.contains("The word was: cat"));
            assertTrue(out.contains("You lost!"));
            assertTrue(out.contains("The word was: dog"));
        }
    }

    static int count(String haystack, String needle) {
        int n = 0;
        for (int i = haystack.indexOf(needle); i >= 0; i = haystack.indexOf(needle, i + 1)) n++;
        return n;
    }
}
