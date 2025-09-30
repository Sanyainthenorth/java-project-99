package hexlet.code.config;

import io.sentry.Sentry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class SentryConfig {

    private static final Logger logger = LoggerFactory.getLogger(SentryConfig.class);

    @Value("${sentry.dsn:}")
    private String sentryDsn;

    @PostConstruct
    public void init() {
        if (sentryDsn != null && !sentryDsn.isEmpty()) {
            logger.info("Initializing Sentry with DSN: {}", sentryDsn.substring(0, 20) + "...");
            Sentry.init(options -> {
                options.setDsn(sentryDsn);
                options.setEnableExternalConfiguration(true);
            });
            logger.info("Sentry initialized successfully");
        } else {
            logger.warn("Sentry DSN is not configured");
        }
    }
}
