plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "3.3.13"
  kotlin("plugin.spring") version "1.5.31"
  kotlin("plugin.jpa") version "1.5.31"
}

configurations {
  implementation { exclude(module = "spring-boot-graceful-shutdown") }
}

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-devtools")

  implementation("org.springframework.security:spring-security-oauth2-jose")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-webflux")

  implementation("io.springfox:springfox-boot-starter:3.0.0")

  implementation("com.google.code.gson:gson:2.8.8")
  implementation("com.google.guava:guava:31.0.1-jre")
  implementation("com.nimbusds:nimbus-jose-jwt:9.15.2")
  implementation("org.apache.commons:commons-text:1.9")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation(platform("com.amazonaws:aws-java-sdk-bom:1.12.93"))
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:1.0.2")

  runtimeOnly("com.h2database:h2:1.4.200")
  runtimeOnly("org.flywaydb:flyway-core:8.0.2")
  runtimeOnly("org.postgresql:postgresql:42.3.0")

  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("org.awaitility:awaitility-kotlin:4.1.0")
  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
  testImplementation("org.testcontainers:localstack:1.16.1")
  testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(16))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "16"
    }
  }
}
