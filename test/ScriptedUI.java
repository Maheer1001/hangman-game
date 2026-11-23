import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Test double for {@link UI}: feeds scripted guesses and records everything the game shows. */
class ScriptedUI implements UI {
    private final Deque<Character> guesses = new ArrayDeque<>();
    final List<String> maskedStates = new ArrayList<>();
    final List<Integer> attemptStates = new ArrayList<>();
    final List<Set<Character>> guessedSeen = new ArrayList<>();
    Boolean won;
    String revealedWord;
    int resultCalls;

    ScriptedUI(String letters) {
        for (char c : letters.toCharArray()) guesses.add(c);
    }

    @Override
    public String askDifficulty() {
        throw new UnsupportedOperationException("not used by game logic");
    }

    @Override
    public char askLetter(Set<Character> guessed) {
        guessedSeen.add(new HashSet<>(guessed));
        if (guesses.isEmpty()) throw new IllegalStateException("game asked for more guesses than scripted");
        return guesses.poll();
    }

    @Override
    public void showGameState(String word, int attemptsLeft) {
        maskedStates.add(word);
        attemptStates.add(attemptsLeft);
    }

    @Override
    public void showResult(boolean won, String word) {
        this.won = won;
        this.revealedWord = word;
        resultCalls++;
    }

    @Override
    public boolean askReplay() {
        return false;
    }

    int remainingGuesses() {
        return guesses.size();
    }
}
