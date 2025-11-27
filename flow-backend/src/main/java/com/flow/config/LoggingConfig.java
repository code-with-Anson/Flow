package com.flow.config;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.LevelFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.spi.FilterReply;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.logback.ColorConverter;
import org.springframework.boot.logging.logback.ExtendedWhitespaceThrowableProxyConverter;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class LoggingConfig {

    @Value("${spring.application.name:flow-backend}")
    private String appName;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    private static final String LOG_PATH = "logs";
    private static final String CONSOLE_LOG_PATTERN = "%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(%5p) %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n%wEx";
    private static final String FILE_LOG_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS} %5p --- [%t] %-40.40logger{39} : %m%n%wEx";

    @PostConstruct
    public void init() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        // Clear existing configuration if needed, but usually Spring Boot has already
        // initialized some.
        // We will add to it or reset it. Resetting is safer to avoid duplicates.
        context.reset();

        // Register Spring Boot's converters
        Map<String, String> ruleRegistry = new HashMap<>();
        ruleRegistry.put("clr", ColorConverter.class.getName());
        ruleRegistry.put("wEx", ExtendedWhitespaceThrowableProxyConverter.class.getName());
        context.putObject(CoreConstants.PATTERN_RULE_REGISTRY, ruleRegistry);

        // Console Appender
        ConsoleAppender<ILoggingEvent> consoleAppender = createConsoleAppender(context);

        // Info File Appender
        RollingFileAppender<ILoggingEvent> infoFileAppender = createRollingFileAppender(context, "info", Level.INFO);
        AsyncAppender asyncInfoAppender = createAsyncAppender(context, infoFileAppender);

        // Error File Appender
        RollingFileAppender<ILoggingEvent> errorFileAppender = createRollingFileAppender(context, "error", Level.ERROR);
        AsyncAppender asyncErrorAppender = createAsyncAppender(context, errorFileAppender);

        // Root Logger
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);

        if ("dev".equals(activeProfile)) {
            rootLogger.addAppender(consoleAppender);
        }
        rootLogger.addAppender(asyncInfoAppender);
        rootLogger.addAppender(asyncErrorAppender);

        // Project Loggers
        Logger projectLogger = context.getLogger("com.flow");
        if ("dev".equals(activeProfile)) {
            projectLogger.setLevel(Level.DEBUG);
        } else {
            projectLogger.setLevel(Level.INFO);
        }

        // SQL Loggers
        Logger sqlLogger = context.getLogger("com.flow.mapper");
        if ("dev".equals(activeProfile)) {
            sqlLogger.setLevel(Level.DEBUG);
        }
    }

    private ConsoleAppender<ILoggingEvent> createConsoleAppender(LoggerContext context) {
        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setContext(context);
        appender.setName("CONSOLE");

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(CONSOLE_LOG_PATTERN);
        encoder.setCharset(StandardCharsets.UTF_8);
        encoder.start();

        appender.setEncoder(encoder);
        appender.start();
        return appender;
    }

    private RollingFileAppender<ILoggingEvent> createRollingFileAppender(LoggerContext context, String name,
            Level level) {
        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setContext(context);
        appender.setName("FILE_" + name.toUpperCase());
        appender.setFile(LOG_PATH + "/" + name + ".log");

        // Rolling Policy
        SizeAndTimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new SizeAndTimeBasedRollingPolicy<>();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(appender);
        rollingPolicy.setFileNamePattern(LOG_PATH + "/" + name + "/" + name + "-%d{yyyy-MM-dd}.%i.log");
        rollingPolicy.setMaxHistory(30);
        rollingPolicy.setMaxFileSize(ch.qos.logback.core.util.FileSize.valueOf("100MB"));
        rollingPolicy.start();

        appender.setRollingPolicy(rollingPolicy);

        // Encoder
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(FILE_LOG_PATTERN);
        encoder.setCharset(StandardCharsets.UTF_8);
        encoder.start();
        appender.setEncoder(encoder);

        // Filter
        LevelFilter filter = new LevelFilter();
        filter.setLevel(level);
        filter.setOnMatch(FilterReply.ACCEPT);
        if (level == Level.ERROR) {
            filter.setOnMismatch(FilterReply.DENY);
        } else {
            filter.setOnMismatch(FilterReply.NEUTRAL);
        }
        filter.start();
        appender.addFilter(filter);

        appender.start();
        return appender;
    }

    private AsyncAppender createAsyncAppender(LoggerContext context, RollingFileAppender<ILoggingEvent> ref) {
        AsyncAppender appender = new AsyncAppender();
        appender.setContext(context);
        appender.setName("ASYNC_" + ref.getName());
        appender.setQueueSize(256);
        appender.setDiscardingThreshold(0);
        appender.addAppender(ref);
        appender.start();
        return appender;
    }
}
