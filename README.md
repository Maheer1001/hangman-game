# hangman-game

A Java console Hangman game with Easy, Medium and Hard difficulties. Words are loaded from a file.

> Coursework for the University of Southampton Year 1 Programming module (2024–25).

## Design

The game is split into small, swappable parts:

- `HangmanGameBase` is an abstract class holding the shared game loop: guessing, tracking attempts and checking for a win or loss.
- `EasyGame`, `MediumGame` and `HardGame` are subclasses that pick the word list and the number of wrong guesses allowed (10, 8 and 6).
- `WordProvider` is the interface for word sources, and `FileWordProvider` reads words by difficulty from `words.txt`.
- `UI` is the interface for input and output, and `ConsoleUI` is the terminal version. It asks for a difficulty and letters, shows the game state and results, and offers a replay.

With the word source and UI behind interfaces, a new front end or word list only needs a new implementation. The game logic stays the same.

## Prerequisites

- JDK 17 or newer (uses switch expressions; tested with JDK 21). Check with `java -version` and `javac -version`.
- On Windows, install one with `winget install --id EclipseAdoptium.Temurin.21.JDK -e`.

No external libraries, build tool or API keys are needed to play. The tests additionally use Maven (see Testing).

## Run

From this folder (so `words.txt` can be found), in PowerShell or Git Bash:

```bash
javac -d out src/*.java
java -cp out Main
```

Choose a difficulty (`easy`, `medium`, `hard`), guess one letter at a time, and answer `yes` or `no` to play again.

Optionally pass a different word list: `java -cp out Main path/to/words.txt` (same `Word Difficulty` format).

Compiled classes go to `out/`, which is gitignored.

## Testing

The tests use JUnit 5 and need [Maven](https://maven.apache.org/download.cgi) 3.6+ on top of the JDK (`mvn -v` to check; on Windows `winget install --id Apache.Maven -e`). Maven downloads JUnit into your local `~/.m2` cache on the first run; nothing is installed globally. Playing the game still needs only `javac`.

From this folder, run everything with one command:

```bash
mvn test
```

You should see `Tests run: 24, Failures: 0, Errors: 0` and `BUILD SUCCESS`. Build output goes to `target/`, which is gitignored. Run one class with `mvn test -Dtest=MainSetupCheckTest`.

What each test class proves (all in `test/`):

| Class | What it checks |
| --- | --- |
| `MainSetupCheckTest` | **Setup check.** Plays full games through the real `Main` entry point with typed input piped in: a win, a loss, invalid difficulty and letter input being re-asked, a replay into a second game, and a run against the bundled `words.txt`. If this passes, the UI, word file and game classes are all hooked together. |
| `HangmanGameBaseTest` | Game logic with a scripted UI: correct and wrong guesses, attempts only lost on a miss, repeated guesses cost nothing, win and lose (including winning on the last attempt), the masked word display, 10/8/6 attempts per difficulty, random word choice from the list, and state reset between games. |
| `ConsoleUITest` | Console input rules: difficulty is case-insensitive and re-asked when invalid; letters must be a single unguessed A-Z (case-insensitive); only `yes` replays; state and result text. |
| `FileWordProviderTest` | Word file parsing: filtering by difficulty, lowercasing, skipping malformed lines, a missing file giving an empty list, and that `words.txt` has 10 clean words per difficulty. |

GitHub Actions (`.github/workflows/tests.yml`) runs `mvn test` and the plain `javac` build on every push and pull request.

A quick manual smoke test without Maven, in Git Bash, pipes a full game's input and should end with `You won!` or `You lost!`:

```bash
javac -d out src/*.java
{ echo easy; for l in e a r i o t n s l c u d p m h g b f y w k v x z j q; do echo $l; done; echo no; } | java -cp out Main
```
