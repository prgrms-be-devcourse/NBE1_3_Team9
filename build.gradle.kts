plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.3.4"
	id("io.spring.dependency-management") version "1.1.6"
	kotlin("plugin.jpa") version "1.9.25"
	kotlin("kapt") version "1.9.25"
}

group = "com.grepp"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Spring Boot
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("com.mysql:mysql-connector-j")

	// Test
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// Swagger
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
  
	// Google Cloud Vision API
	implementation("com.google.cloud:google-cloud-vision:3.50.0")

	// JSON
	implementation("org.json:json:20231013")

	// Jackson Databind
	implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")

	// JWT
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
	implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")

	// OAuth 2.0
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("io.netty:netty-resolver-dns-native-macos:4.1.92.Final:osx-aarch_64")
	testImplementation("io.projectreactor:reactor-test")

	/// QueryDSL
	implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
	kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")
	kapt("jakarta.annotation:jakarta.annotation-api")
	kapt("jakarta.persistence:jakarta.persistence-api")

	implementation("io.github.christian-draeger:k6-kotlin:0.1.0")

	implementation ("org.apache.httpcomponents.client5:httpclient5:5.2")

	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("io.micrometer:micrometer-registry-prometheus")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

sourceSets {
	main {
		java {
			srcDirs("src/main/java", "src/main/kotlin", "src/main/generated")
		}
	}
}

tasks.named("clean") {
	delete(file("src/main/generated"))
}

tasks.getByName<Jar>("bootJar") {
	archiveFileName.set("app.jar")
}


tasks.getByName<Jar>("jar") {
	enabled = false
}

tasks.withType<Test> {
	useJUnitPlatform()
}

springBoot {
	mainClass.set("com.grepp.nbe1_3_team9.Nbe13Team9ApplicationKt")
}
