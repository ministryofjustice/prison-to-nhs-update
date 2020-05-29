plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "0.4.0"
}

extra["spring-security.version"] = "5.3.2.RELEASE" // Required for CVE-2018-1258

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

  implementation("io.springfox:springfox-swagger2:2.9.2")
  implementation("io.springfox:springfox-swagger-ui:2.9.2")
  implementation("io.springfox:springfox-bean-validators:2.9.2")

  implementation( "com.google.code.gson:gson:2.8.6")
  implementation("com.google.guava:guava:28.2-jre")
  implementation("com.nimbusds:nimbus-jose-jwt:8.11")

  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.10.3")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.springframework:spring-jms")
  implementation( platform ("com.amazonaws:aws-java-sdk-bom:1.11.750"))
  implementation("com.amazonaws:amazon-sqs-java-messaging-lib:1.0.8")

  runtimeOnly("com.h2database:h2:1.4.200")
  runtimeOnly("org.flywaydb:flyway-core:6.2.4")
  runtimeOnly("org.postgresql:postgresql:42.2.10")

  testImplementation("org.springframework.boot:spring-boot-starter-webflux")
  testImplementation("com.github.tomakehurst:wiremock-standalone:2.26.3")
  testImplementation("org.testcontainers:localstack:1.13.0")
  testImplementation("org.awaitility:awaitility-kotlin:4.0.2")
  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
}
