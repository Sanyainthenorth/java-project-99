package hexlet.code.controller;

import io.sentry.Sentry;
import io.sentry.protocol.SentryId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @GetMapping("/test-sentry")
    public String testSentry() {
        logger.info("=== SENTRY TEST STARTED ===");

        try {
            // Проверяем, инициализирован ли Sentry
            boolean sentryEnabled = Sentry.isEnabled();
            logger.info("Sentry enabled: {}", sentryEnabled);

            if (!sentryEnabled) {
                return "Sentry is not enabled! Check configuration.";
            }

            // Создаем ошибку
            throw new RuntimeException("Sentry test error - timestamp: " + System.currentTimeMillis());

        } catch (Exception e) {
            logger.error("Capturing exception to Sentry", e);

            // Отправляем в Sentry (возвращает SentryId)
            SentryId eventId = Sentry.captureException(e);
            logger.info("Exception captured with event ID: {}", eventId);

            // Также отправляем тестовое сообщение
            SentryId messageId = Sentry.captureMessage("Test message from Spring Boot - " + System.currentTimeMillis());
            logger.info("Message captured with ID: {}", messageId);

            return String.format("Error sent to Sentry! Event IDs: %s, %s", eventId, messageId);
        }
    }
}

