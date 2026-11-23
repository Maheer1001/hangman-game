import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileWordProviderTest {

    @TempDir
    Path dir;

    private FileWordProvider providerFor(String content) throws IOException {
        Path file = dir.resolve("words.txt");
        Files.writeString(file, content);
        return new FileWordProvider(file.toString());
    }

    @Test
    void returnsOnlyWordsForTheRequestedDifficultyLowercased() throws IOException {
        FileWordProvider p = providerFor("Apple Easy\r\nBanana Medium\nCherry easy\nDurian HARD\n");

        assertEquals(List.of("apple", "cherry"), p.getWords("easy"));
        assertEquals(List.of("banana"), p.getWords("medium"));
        assertEquals(List.of("durian"), p.getWords("hard"));
    }

    @Test
    void skipsMalformedLines() throws IOException {
        FileWordProvider p = providerFor("\nlonely\ntoo many parts Easy\nGood Easy\n");

        assertEquals(List.of("good"), p.getWords("easy"));
    }

    @Test
    void missingFileGivesEmptyListAndAnError() {
        FileWordProvider p = new FileWordProvider(dir.resolve("nope.txt").toString());
        PrintStream originalErr = System.err;
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err, true));
        try {
            assertEquals(List.of(), p.getWords("easy"));
        } finally {
            System.setErr(originalErr);
        }
        assertTrue(err.toString().contains("Error reading file"));
    }

    @Test
    void bundledWordListHasWordsForEveryDifficulty() {
        // Tests run from the repo root, the same folder the game expects to be launched from.
        FileWordProvider p = new FileWordProvider("words.txt");
        for (String d : List.of("easy", "medium", "hard")) {
            List<String> words = p.getWords(d);
            assertEquals(10, words.size(), d);
            for (String w : words) {
                assertTrue(w.matches("[a-z]+"), d + " word should be plain lowercase letters: " + w);
            }
        }
    }
}
