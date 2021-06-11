package uk.gov.justice.digital.hmpps.prisontonhs.services.health

import com.amazonaws.services.sqs.model.GetQueueAttributesResult
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.justice.digital.hmpps.prisontonhs.config.SqsConfigProperties
import uk.gov.justice.digital.hmpps.prisontonhs.integration.IntegrationTest

@ExtendWith(SpringExtension::class)
class HealthCheckIntegrationTest : IntegrationTest() {
  @Autowired
  private lateinit var sqsConfigProperties: SqsConfigProperties
  private var queueName: String? = null
  private var dlqName: String? = null

  @BeforeEach
  fun setup() {
    queueName = sqsConfigProperties.queueName
    dlqName = sqsConfigProperties.dlqName
  }

  @AfterEach
  fun tearDown() {
    ReflectionTestUtils.setField(sqsConfigProperties, "queueName", queueName)
    ReflectionTestUtils.setField(sqsConfigProperties, "dlqName", dlqName)
  }

  @Test
  fun `Health page reports ok`() {
    subPing(200)

    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody()
      .jsonPath("components.oauthApiHealth.details.HttpStatus").isEqualTo("OK")
      .jsonPath("components.nomisApiHealth.details.HttpStatus").isEqualTo("OK")
      .jsonPath("components.prisonRegisterApiHealth.details.HttpStatus").isEqualTo("OK")
      .jsonPath("components.db.status").isEqualTo("UP")
      .jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `Health ping page is accessible`() {
    subPing(200)

    webTestClient.get()
      .uri("/health/ping")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `Health page reports down`() {
    subPing(404)

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
    subPing(418)

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
  fun `Queue does not exist reports down`() {
    ReflectionTestUtils.setField(sqsConfigProperties, "queueName", "missing_queue")
    subPing(200)

    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .is5xxServerError
      .expectBody()
      .jsonPath("components.queueHealth.status").isEqualTo("DOWN")
      .jsonPath("status").isEqualTo("DOWN")
  }

  @Test
  fun `Queue health ok and dlq health ok, reports everything up`() {
    subPing(200)

    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("components.queueHealth.status").isEqualTo("UP")
      .jsonPath("components.queueHealth.status").isEqualTo(DlqStatus.UP.description)
      .jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `Dlq health reports interesting attributes`() {
    subPing(200)

    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("components.queueHealth.details.${QueueAttributes.MESSAGES_ON_DLQ.healthName}").isEqualTo(0)
  }

  @Test
  fun `Dlq down brings main health and queue health down`() {
    subPing(200)
    mockQueueWithoutRedrivePolicyAttributes()

    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .is5xxServerError
      .expectBody()
      .jsonPath("status").isEqualTo("DOWN")
      .jsonPath("components.queueHealth.status").isEqualTo("DOWN")
      .jsonPath("components.queueHealth.details.dlqStatus").isEqualTo(DlqStatus.NOT_ATTACHED.description)
  }

  @Test
  fun `Main queue has no redrive policy reports dlq down`() {
    subPing(200)
    mockQueueWithoutRedrivePolicyAttributes()

    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .is5xxServerError
      .expectBody()
      .jsonPath("components.queueHealth.details.dlqStatus").isEqualTo(DlqStatus.NOT_ATTACHED.description)
  }

  @Test
  fun `Dlq not found reports dlq down`() {
    subPing(200)
    ReflectionTestUtils.setField(sqsConfigProperties, "dlqName", "missing_queue")

    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .is5xxServerError
      .expectBody()
      .jsonPath("components.queueHealth.details.dlqStatus").isEqualTo(DlqStatus.NOT_FOUND.description)
  }

  @Test
  fun `Health liveness page is accessible`() {
    webTestClient.get().uri("/health/liveness")
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `Health readiness page is accessible`() {
    webTestClient.get().uri("/health/readiness")
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("status").isEqualTo("UP")
  }

  private fun subPing(status: Int) {
    oauthMockServer.stubFor(
      get("/auth/health/ping").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(if (status == 200) "pong" else "some error")
          .withStatus(status)
      )
    )

    prisonMockServer.stubFor(
      get("/health/ping").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(if (status == 200) "pong" else "some error")
          .withStatus(status)
      )
    )

    prisonRegisterMockServer.stubFor(
      get("/health/ping").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(if (status == 200) "pong" else "some error")
          .withStatus(status)
      )
    )
  }

  private fun mockQueueWithoutRedrivePolicyAttributes() {
    doReturn(GetQueueAttributesResult()).`when`(awsSqsClient).getQueueAttributes(any())
  }
}
