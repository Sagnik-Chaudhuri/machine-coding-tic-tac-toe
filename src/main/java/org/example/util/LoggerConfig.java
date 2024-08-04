package org.example.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerConfig {
    public static void configureLogger(Logger logger) {
        logger.setLevel(Level.ALL);
    }
}
