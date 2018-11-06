package pt.fabm;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;

import java.io.File;

public class LoggerInitializer {
    private static LoggerInitializer loggerInitializer = new LoggerInitializer();
    private File file;
    private FileAppender<ILoggingEvent> fileAppender;

    public static LoggerInitializer getLoggerInitializer() {
        return loggerInitializer;
    }

    public void setFile(File file) {
        this.file = file;
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder ple = new PatternLayoutEncoder();

        ple.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
        ple.setContext(lc);
        ple.start();
        fileAppender = new FileAppender<ILoggingEvent>();
        fileAppender.setFile(file.getAbsolutePath());
        fileAppender.setEncoder(ple);
        fileAppender.setContext(lc);
        fileAppender.start();
    }

    public Logger createLogger(Class<?> klass) {
        return createLogger(klass.getName());
    }

    public Logger createLogger(String string) {
        Logger logger = (Logger) LoggerFactory.getLogger(string);
        logger.addAppender(fileAppender);
        logger.setLevel(Level.DEBUG);
        logger.setAdditive(false); /* set to true if root should log too */
        return logger;
    }
}
