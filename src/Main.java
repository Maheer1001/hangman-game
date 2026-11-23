public class Main {
    public static void main(String[] args) {
        // Optional first argument: path to a word list (defaults to words.txt; tests use it to fix the word).
        String wordsFile = args.length > 0 ? args[0] : "words.txt";
        WordProvider wordProvider = new FileWordProvider(wordsFile);
        UI ui = new ConsoleUI();

        while (true) {
            String difficulty = ui.askDifficulty();
            HangmanGameBase game = switch (difficulty) {
                case "easy" -> new EasyGame(wordProvider, ui);
                case "medium" -> new MediumGame(wordProvider, ui);
                case "hard" -> new HardGame(wordProvider, ui);
                default -> throw new IllegalArgumentException("Invalid difficulty: " + difficulty);
            };
            game.start();
            if (!ui.askReplay()) break;
        }
    }
}