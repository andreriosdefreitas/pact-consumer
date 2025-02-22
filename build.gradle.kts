import org.jetbrains.kotlin.js.translate.context.Namer.kotlin
import java.io.ByteArrayOutputStream

plugins {
	id("org.springframework.boot") version "3.2.6"
	id("io.spring.dependency-management") version "1.1.5"
	kotlin("jvm") version "1.9.24"
	kotlin("plugin.spring") version "1.9.24"
	id("au.com.dius.pact") version "4.6.17"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
	maven { url = uri("https://registry.us.gympass.cloud/repository/maven-public/") }
}

dependencies {
	implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2023.0.0"))
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("au.com.dius.pact.consumer:junit5:4.6.10")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

pact {
	broker {
		pactBrokerUrl = System.getenv("PACT_BROKER_URL")
		pactBrokerToken = System.getenv("PACT_BROKER_TOKEN")
	}
	publish {
		pactBrokerUrl = System.getenv("PACT_BROKER_URL")
		pactBrokerToken = System.getenv("PACT_BROKER_TOKEN")
		consumerVersion = getGitHash()
		consumerBranch = getGitBranch()
	}
}

fun getGitHash(): String {
	val stdout = ByteArrayOutputStream()
	exec {
		commandLine = listOf("git", "rev-parse", "--short", "HEAD")
		standardOutput = stdout
	}
	return stdout.toString().trim()
}

fun getGitBranch(): String {
	val stdout = ByteArrayOutputStream()
	exec {
		commandLine = listOf("git", "rev-parse", "--abbrev-ref", "HEAD")
		standardOutput = stdout
	}
	return stdout.toString().trim()
}
