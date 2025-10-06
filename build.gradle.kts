plugins {
	java
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.sonarqube") version "4.4.1.3373"
	id("jacoco")
	id("io.freefair.lombok") version "8.13.1"

}

group = "hexlet.code"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("com.nimbusds:nimbus-jose-jwt:9.31")
	implementation("org.openapitools:jackson-databind-nullable:0.2.6")
	implementation ("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")
	// Spring Security Crypto для хеширования паролей
	implementation("org.springframework.security:spring-security-crypto")

	// MapStruct для автоматического маппинга
	implementation("org.mapstruct:mapstruct:1.5.5.Final")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

	// BCrypt для хеширования
	implementation("org.springframework.security:spring-security-core")

	// Для тестов
	testImplementation("org.springframework.security:spring-security-test")

	// H2 Database (для разработки)
	runtimeOnly("com.h2database:h2")

	// PostgreSQL (для продакшена)
	runtimeOnly("org.postgresql:postgresql")

	implementation ("io.sentry:sentry-spring-boot-starter:6.27.0")
}
sonarqube {
	properties {
		property("sonar.projectKey", "Sanyainthenorth_java-project-99")
		property("sonar.organization", "sanyainthenorth")
		property("sonar.host.url", "https://sonarcloud.io")
		property("sonar.token", System.getenv("SONAR_TOKEN"))
		property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.jacocoTestReport {
	reports {
		xml.required = true
		html.required = true
	}
}

tasks.test {
	finalizedBy(tasks.jacocoTestReport)
}
