import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Writes user events to a text file using a separate logging thread.
 * Each event is appended with the date, filename, time, and action.
 */
public class Logging {
    private static final Path LOG_FILE = Path.of("grpaint-events.log").toAbsolutePath();
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    // THREADING: Use a single thread to write to the log file so that events are always in order.
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "GRPaint-Logging");
        thread.setDaemon(false);
        return thread;
    });

    /**
     * Adds a single user event to the log without blocking the drawing tools.
     * @param filename the image name or tab name
     * @param action a description of what the user did
     */
    public static void log(String filename, String action) {
        LocalDateTime now = LocalDateTime.now();
        String file = clean(filename == null || filename.isBlank() ? "new tab" : filename);
        String event = clean(action);
        String line = DATE.format(now) + " [" + file + "] " + TIME.format(now) + " " + event
                + System.lineSeparator();
        if (WORKER.isShutdown()) return;
        try {
            WORKER.execute(() -> {
                try {
                    Files.writeString(LOG_FILE, line, StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                } catch (IOException ex) {
                    System.err.println("Could not write GRPaint event log: " + ex.getMessage());
                }
            });
        } catch (java.util.concurrent.RejectedExecutionException ignored) {
        }
    }

    /**
     * Gets the location of the activity log.
     * @return the full path to the log file
     */
    public static Path getLogFile() {
        return LOG_FILE;
    }

    /**
     * Waits for queued messages to finish when GRPaint closes.
     */
    public static void close() {
        WORKER.shutdown();
        try {
            if (!WORKER.awaitTermination(5, TimeUnit.SECONDS)) {
                WORKER.shutdownNow();
                System.err.println("GRPaint logging timed out while closing.");
            }
        } catch (InterruptedException ex) {
            WORKER.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Removes line breaks so one action always uses one log line.
     * @param value the text being prepared
     * @return a safe single-line value
     */
    private static String clean(String value) {
        return value == null ? "" : value.replace('\n', ' ').replace('\r', ' ').replace(']', ')');
    }
}
