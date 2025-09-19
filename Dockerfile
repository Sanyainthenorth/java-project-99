FROM openjdk:21-jdk-slim

WORKDIR /app

# Копируем только нужные для сборки файлы
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Копируем исходный код
COPY src ./src

# Даем права на выполнение gradlew
RUN chmod +x gradlew

# Скачиваем зависимости и собираем приложение (кешируем зависимости)
RUN ./gradlew build -x test --no-daemon

# Открываем порт (Render использует порт 10000)
EXPOSE 10000

# Запускаем приложение
CMD ["java", "-jar", "build/libs/app-0.0.1-SNAPSHOT.jar"]