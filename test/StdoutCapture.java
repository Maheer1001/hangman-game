import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/** Swaps System.in / System.out for a test and restores them on close. */
class StdoutCapture implements AutoCloseable {
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    StdoutCapture() {
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    /** Feeds the given lines to System.in, one per line, as if typed by a player. */
    StdoutCapture withInput(String... lines) {
        String text = String.join(System.lineSeparator(), lines) + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
        return this;
    }

    String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Override
    public void close() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }
}
