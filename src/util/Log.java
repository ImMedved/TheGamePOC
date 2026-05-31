package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class Log {

    private static final Logger LOGGER = Logger.getLogger("thegame");

    static {
        configure();
    }

    private Log() {
    }

    public static void debug(String message) {
        LOGGER.log(Level.FINE, message);
    }

    public static boolean isDebugEnabled() {
        return LOGGER.isLoggable(Level.FINE);
    }

    public static void info(String message) {
        LOGGER.log(Level.INFO, message);
    }

    public static void warn(String message) {
        LOGGER.log(Level.WARNING, message);
    }

    public static void error(String message, Throwable error) {
        LOGGER.log(Level.SEVERE, message, error);
    }

    private static void configure() {
        Level level = parseLevel(System.getenv().getOrDefault("LOG_LEVEL", "INFO"));
        Logger root = Logger.getLogger("");

        for (Handler handler : root.getHandlers()) {
            root.removeHandler(handler);
        }

        ConsoleHandler console = new ConsoleHandler();
        console.setLevel(level == Level.FINE ? Level.INFO : level);
        root.addHandler(console);

        if (level == Level.FINE) {
            addDebugFileHandler(root);
        }

        root.setLevel(level);
        LOGGER.setLevel(level);
    }

    private static void addDebugFileHandler(Logger root) {
        String logFile = System.getenv().getOrDefault("LOG_FILE", "logs/debug.log");
        Path path = Path.of(logFile);

        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            FileHandler file = new FileHandler(path.toString(), true);
            file.setLevel(Level.FINE);
            file.setFormatter(new SimpleFormatter());
            root.addHandler(file);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to configure debug log file: " + path, e);
        }
    }

    private static Level parseLevel(String value) {
        return switch (value.toUpperCase(Locale.ROOT)) {
            case "DEBUG", "FINE" -> Level.FINE;
            case "WARN", "WARNING" -> Level.WARNING;
            case "ERROR", "SEVERE" -> Level.SEVERE;
            default -> Level.INFO;
        };
    }
}
