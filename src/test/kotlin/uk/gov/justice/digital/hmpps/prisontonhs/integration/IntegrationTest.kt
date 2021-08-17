package uk.gov.justice.digital.hmpps.prisontonhs.integration

import com.google.gson.Gson
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.prisontonhs.integration.LocalStackContainer.setLocalStackProperties
import uk.gov.justice.digital.hmpps.prisontonhs.integration.wiremock.NhsExtension
import uk.gov.justice.digital.hmpps.prisontonhs.integration.wiremock.OAuthExtension
import uk.gov.justice.digital.hmpps.prisontonhs.integration.wiremock.PrisonExtension
import uk.gov.justice.digital.hmpps.prisontonhs.integration.wiremock.PrisonRegisterExtension
import uk.gov.justice.digital.hmpps.prisontonhs.services.JwtAuthHelper
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService

@ExtendWith(OAuthExtension::class, PrisonExtension::class, PrisonRegisterExtension::class, NhsExtension::class)
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class IntegrationTest {

  @Autowired
  protected lateinit var hmppsQueueService: HmppsQueueService

  internal val eventQueue by lazy { hmppsQueueService.findByQueueId("event") as HmppsQueue }
  internal val awsSqsClient by lazy { eventQueue.sqsClient }
  internal val eventQueueName by lazy { eventQueue.queueName }
  internal val queueUrl by lazy { eventQueue.queueUrl }
  internal val eventDlqName by lazy { eventQueue.dlqName as String }

  @Autowired
  private lateinit var gson: Gson

  @Autowired
  internal lateinit var webTestClient: WebTestClient

  @Autowired
  internal lateinit var jwtAuthHelper: JwtAuthHelper

  companion object {
    private val localStackContainer = LocalStackContainer.instance

    @JvmStatic
    @DynamicPropertySource
    fun testcontainers(registry: DynamicPropertyRegistry) {
      localStackContainer?.also { setLocalStackProperties(it, registry) }
    }
  }

  internal fun Any.asJson() = gson.toJson(this)

  internal fun setAuthorisation(
    user: String = "prison-to-nhs-api-client",
    roles: List<String> = listOf(),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisation(user, roles)
}
