FROM openjdk:21-jdk-slim

WORKDIR /app

# Копируем Gradle wrapper и конфиги
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .

# Копируем исходный код
COPY src ./src

# Даем права на выполнение gradlew
RUN chmod +x gradlew

# Собираем приложение (пропускаем тесты - они в CI)
RUN ./gradlew build -x test

# Открываем порт (Render использует порт 10000)
EXPOSE 10000

# Запускаем приложение
CMD ["java", "-jar", "build/libs/app-0.0.1-SNAPSHOT.jar"]
