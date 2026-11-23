import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class HangmanGameBaseTest {

    /** Word source that always returns the given words and remembers which difficulty was asked for. */
    static class FixedWords implements WordProvider {
        final List<String> words;
        final List<String> requested = new ArrayList<>();

        FixedWords(String... words) { this.words = List.of(words); }

        @Override
        public List<String> getWords(String difficulty) {
            requested.add(difficulty);
            return words;
        }
    }

    /** Plays one game with console feedback ("Correct!" etc.) captured; returns that output. */
    private static String play(HangmanGameBase game) {
        try (StdoutCapture cap = new StdoutCapture()) {
            game.start();
            return cap.output();
        }
    }

    @Test
    void correctGuessesWinTheGame() {
        ScriptedUI ui = new ScriptedUI("cat");
        play(new EasyGame(new FixedWords("cat"), ui));

        assertEquals(Boolean.TRUE, ui.won);
        assertEquals("cat", ui.revealedWord);
        assertEquals(1, ui.resultCalls);
        assertEquals(0, ui.remainingGuesses(), "game should stop as soon as the word is complete");
    }

    @Test
    void displayRevealsLettersAsTheyAreGuessed() {
        ScriptedUI ui = new ScriptedUI("tca");
        play(new EasyGame(new FixedWords("cat"), ui));

        assertEquals(List.of("___", "__t", "c_t"), ui.maskedStates);
    }

    @Test
    void repeatedLetterInWordIsRevealedEverywhereAtOnce() {
        ScriptedUI ui = new ScriptedUI("lev");
        play(new EasyGame(new FixedWords("level"), ui));

        assertEquals(List.of("_____", "l___l", "le_el"), ui.maskedStates);
        assertTrue(ui.won);
    }

    @Test
    void wrongGuessCostsAnAttemptAndCorrectGuessDoesNot() {
        ScriptedUI ui = new ScriptedUI("xcat");
        play(new EasyGame(new FixedWords("cat"), ui));

        // states shown before each guess: start, after x (wrong), after c, after a
        assertEquals(List.of(10, 9, 9, 9), ui.attemptStates);
        assertTrue(ui.won);
    }

    @Test
    void runningOutOfAttemptsLosesTheGame() {
        ScriptedUI ui = new ScriptedUI("bdefgh"); // 6 wrong letters for "cat"
        play(new HardGame(new FixedWords("cat"), ui));

        assertEquals(Boolean.FALSE, ui.won);
        assertEquals("cat", ui.revealedWord, "the word is revealed on a loss");
        assertEquals(List.of(6, 5, 4, 3, 2, 1), ui.attemptStates);
        assertEquals(0, ui.remainingGuesses());
    }

    @Test
    void canStillWinOnTheLastAttempt() {
        ScriptedUI ui = new ScriptedUI("bdefgcat"); // 5 wrong, then the word
        play(new HardGame(new FixedWords("cat"), ui));

        assertTrue(ui.won);
        assertEquals(1, ui.attemptStates.get(ui.attemptStates.size() - 1));
    }

    @Test
    void repeatedGuessIsReportedAndCostsNothing() {
        // ConsoleUI normally blocks repeats, but the game itself must also cope if a UI lets one through.
        ScriptedUI ui = new ScriptedUI("xxcat");
        String out = play(new EasyGame(new FixedWords("cat"), ui));

        assertTrue(out.contains("You have already guessed this!"));
        assertEquals(List.of(10, 9, 9, 9, 9), ui.attemptStates, "second x must not cost an attempt");
        assertTrue(ui.won);
    }

    @Test
    void feedbackMessagesArePrinted() {
        ScriptedUI ui = new ScriptedUI("cxat");
        String out = play(new EasyGame(new FixedWords("cat"), ui));

        assertTrue(out.contains("Correct!"));
        assertTrue(out.contains("Incorrect!"));
    }

    @Test
    void uiIsGivenTheLettersGuessedSoFar() {
        ScriptedUI ui = new ScriptedUI("cat");
        play(new EasyGame(new FixedWords("cat"), ui));

        assertEquals(List.of(Set.of(), Set.of('c'), Set.of('c', 'a')), ui.guessedSeen);
    }

    @Test
    void eachDifficultyAsksForItsWordsAndGivesItsAttempts() {
        Map<String, Integer> expected = Map.of("easy", 10, "medium", 8, "hard", 6);
        for (Map.Entry<String, Integer> e : expected.entrySet()) {
            FixedWords words = new FixedWords("cat");
            ScriptedUI ui = new ScriptedUI("cat");
            HangmanGameBase game = switch (e.getKey()) {
                case "easy" -> new EasyGame(words, ui);
                case "medium" -> new MediumGame(words, ui);
                default -> new HardGame(words, ui);
            };
            play(game);
            assertEquals(List.of(e.getKey()), words.requested, e.getKey());
            assertEquals(e.getValue(), ui.attemptStates.get(0), e.getKey());
        }
    }

    @Test
    void wordIsPickedFromTheProvidersList() {
        String[] options = {"cat", "dog", "owl"};
        Set<String> picked = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            ScriptedUI ui = new ScriptedUI("catdgowl"); // enough letters to finish any option
            play(new EasyGame(new FixedWords(options), ui));
            assertTrue(ui.won);
            picked.add(ui.revealedWord);
        }
        assertTrue(Set.of(options).containsAll(picked), "only listed words are used");
        // Random pick: chance of missing a word in 200 games is about 3 * (2/3)^200, effectively zero.
        assertEquals(3, picked.size(), "over 200 games every word should come up");
    }

    @Test
    void startingAgainResetsGuessesAndAttempts() {
        ScriptedUI ui = new ScriptedUI("xcat" + "cat");
        HardGame game = new HardGame(new FixedWords("cat"), ui);
        play(game);
        ui.maskedStates.clear();
        ui.attemptStates.clear();
        play(game);

        assertEquals("___", ui.maskedStates.get(0), "letters from the previous game are forgotten");
        assertEquals(6, ui.attemptStates.get(0), "attempts are refilled");
        assertEquals(2, ui.resultCalls);
    }
}
