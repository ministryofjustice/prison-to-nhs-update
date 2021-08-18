package uk.gov.justice.digital.hmpps.prisontonhs.services.health

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.prisontonhs.integration.IntegrationTest
import uk.gov.justice.digital.hmpps.prisontonhs.integration.wiremock.NhsExtension
import uk.gov.justice.digital.hmpps.prisontonhs.integration.wiremock.OAuthExtension
import uk.gov.justice.digital.hmpps.prisontonhs.integration.wiremock.PrisonExtension
import uk.gov.justice.digital.hmpps.prisontonhs.integration.wiremock.PrisonRegisterExtension

@ExtendWith(SpringExtension::class)
class HealthCheckTest : IntegrationTest() {

  @Test
  fun `Health page reports ok`() {
    stubPingWithResponse(200)

    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("components.oauthApiHealth.details.HttpStatus").isEqualTo("OK")
      .jsonPath("components.nomisApiHealth.details.HttpStatus").isEqualTo("OK")
      .jsonPath("components.prisonRegisterApiHealth.details.HttpStatus").isEqualTo("OK")
      .jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `Health ping page is accessible`() {
    stubPingWithResponse(200)

    webTestClient.get()
      .uri("/health/ping")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `readiness reports ok`() {
    stubPingWithResponse(200)

    webTestClient.get()
      .uri("/health/readiness")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `liveness reports ok`() {
    stubPingWithResponse(200)

    webTestClient.get()
      .uri("/health/liveness")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `Health page reports down`() {
    stubPingWithResponse(404)

    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .is5xxServerError
      .expectBody()
      .jsonPath("status").isEqualTo("DOWN")
      .jsonPath("components.oauthApiHealth.details.HttpStatus").isEqualTo("NOT_FOUND")
      .jsonPath("components.nomisApiHealth.details.HttpStatus").isEqualTo("NOT_FOUND")
      .jsonPath("components.prisonRegisterApiHealth.details.HttpStatus").isEqualTo("NOT_FOUND")
  }

  @Test
  fun `Health page reports a teapot`() {
    stubPingWithResponse(418)

    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .is5xxServerError
      .expectBody()
      .jsonPath("components.oauthApiHealth.details.HttpStatus").isEqualTo("I_AM_A_TEAPOT")
      .jsonPath("components.nomisApiHealth.details.HttpStatus").isEqualTo("I_AM_A_TEAPOT")
      .jsonPath("components.prisonRegisterApiHealth.details.HttpStatus").isEqualTo("I_AM_A_TEAPOT")
      .jsonPath("status").isEqualTo("DOWN")
  }

  @Test
  fun `Event Queue health reports queue details`() {
    stubPingWithResponse(200)

    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("components.event-health.status").isEqualTo("UP")
      .jsonPath("components.event-health.details.queueName").isEqualTo(eventQueueName)
      .jsonPath("components.event-health.details.messagesOnQueue").isEqualTo(0)
      .jsonPath("components.event-health.details.messagesInFlight").isEqualTo(0)
      .jsonPath("components.event-health.details.messagesOnDlq").isEqualTo(0)
      .jsonPath("components.event-health.details.dlqStatus").isEqualTo("UP")
      .jsonPath("components.event-health.details.dlqName").isEqualTo(eventDlqName)
  }

  private fun stubPingWithResponse(status: Int) {
    OAuthExtension.oAuthApi.stubHealthPing(status)
    PrisonExtension.prisonApi.stubHealthPing(status)
    PrisonRegisterExtension.prisonRegisterApi.stubHealthPing(status)
    NhsExtension.nhsApi.stubHealthPing(status)
  }
}
