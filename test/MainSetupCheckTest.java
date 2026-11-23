import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Setup check: plays whole games through the real entry point (Main -> ConsoleUI -> FileWordProvider ->
 * game classes) with typed-in input, exactly as a player would. If these pass, everything is hooked together.
 */
class MainSetupCheckTest {

    @TempDir
    Path dir;

    /** One known word per difficulty so the scripted input is deterministic. */
    private String fixedWordsFile() throws IOException {
        Path file = dir.resolve("words.txt");
        Files.writeString(file, "Cat Easy\nOwl Medium\nZebra Hard\n");
        return file.toString();
    }

    private static String run(String[] args, String... inputLines) {
        try (StdoutCapture cap = new StdoutCapture().withInput(inputLines)) {
            Main.main(args);
            return cap.output();
        }
    }

    @Test
    void winPath() throws IOException {
        String out = run(new String[]{fixedWordsFile()}, "easy", "c", "x", "a", "t", "no");

        assertTrue(out.contains("Welcome to Hangman"));
        assertTrue(out.contains("Your word is: ___"));
        assertTrue(out.contains("Attempts left: 10"));
        assertTrue(out.contains("Attempts left: 9"), "the wrong guess x cost one attempt");
        assertTrue(out.contains("You won!"));
        assertTrue(out.contains("The word was: cat"));
        assertFalse(out.contains("You lost!"));
    }

    @Test
    void losePath() throws IOException {
        String out = run(new String[]{fixedWordsFile()}, "hard", "q", "w", "y", "u", "i", "o", "no");

        assertTrue(out.contains("Attempts left: 6"));
        assertTrue(out.contains("Attempts left: 1"));
        assertTrue(out.contains("You lost!"));
        assertTrue(out.contains("The word was: zebra"));
        assertFalse(out.contains("You won!"));
    }

    @Test
    void invalidInputIsRejectedThenReplayPlaysASecondGame() throws IOException {
        String out = run(new String[]{fixedWordsFile()},
                "nightmare", "MEDIUM",            // bad difficulty, then valid in caps
                "ow", "1", "o", "o", "W", "l",   // bad letters and a repeat are re-asked, not counted
                "yes",                            // play again
                "hard", "a", "b", "c", "d", "f", "g", "h", "i", // a, b hit; 6 misses -> lose
                "no");

        assertTrue(out.contains("Invalid. Please choose easy, medium, or hard"));
        assertEquals(3, ConsoleUITest.count(out, "Invalid or You have already guessed this!"));
        assertTrue(out.contains("Attempts left: 8"), "medium starts with 8 attempts");
        assertTrue(out.contains("The word was: owl"));
        assertTrue(out.contains("You won!"));
        assertTrue(out.contains("The word was: zebra"));
        assertTrue(out.contains("You lost!"));
        assertEquals(2, ConsoleUITest.count(out, "Welcome to Hangman"), "replay started a second game");
    }

    @Test
    void defaultWordListWorksFromRepoRoot() {
        // No argument: uses the bundled words.txt exactly like `java -cp out Main`.
        String[] alphabet = "e a r i o t n s l c u d p m h g b f y w k v x z j q".split(" ");
        String[] input = new String[alphabet.length + 2];
        input[0] = "easy";
        System.arraycopy(alphabet, 0, input, 1, alphabet.length);
        input[input.length - 1] = "no";

        String out = run(new String[0], input);

        assertTrue(out.contains("You won!") || out.contains("You lost!"), out);
        List<String> easyWords = new FileWordProvider("words.txt").getWords("easy");
        assertTrue(easyWords.stream().anyMatch(w -> out.contains("The word was: " + w)),
                "the word shown must come from the easy list in words.txt");
    }
}
