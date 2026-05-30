package util;

import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        console.setLevel(level);
        root.addHandler(console);
        root.setLevel(level);
        LOGGER.setLevel(level);
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
