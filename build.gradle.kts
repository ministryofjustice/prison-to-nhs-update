plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "3.3.0"
  kotlin("plugin.spring") version "1.5.10"
  kotlin("plugin.jpa") version "1.5.10"
}

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-devtools")

  implementation("org.springframework.security:spring-security-oauth2-jose")
  implementation("org.springframework:spring-webflux")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-reactor-netty")
  implementation("org.springframework.cloud:spring-cloud-aws-messaging:2.2.6.RELEASE")

  implementation("io.springfox:springfox-boot-starter:3.0.0")

  implementation("com.google.code.gson:gson:2.8.7")
  implementation("com.google.guava:guava:30.1.1-jre")
  implementation("com.nimbusds:nimbus-jose-jwt:9.10")
  implementation("org.apache.commons:commons-text:1.9")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.springframework:spring-jms")
  implementation(platform("com.amazonaws:aws-java-sdk-bom:1.12.1"))
  implementation("com.amazonaws:amazon-sqs-java-messaging-lib:1.0.8")

  runtimeOnly("com.h2database:h2:1.4.200")
  runtimeOnly("org.flywaydb:flyway-core:7.9.2")
  runtimeOnly("org.postgresql:postgresql:42.2.20")

  testImplementation("org.springframework.boot:spring-boot-starter-webflux")
  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("org.awaitility:awaitility-kotlin:4.1.0")
  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
}
