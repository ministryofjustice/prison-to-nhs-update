package uk.gov.justice.digital.hmpps.prisontonhs.integration

import com.amazonaws.services.sqs.AmazonSQS
import com.google.gson.Gson
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.prisontonhs.integration.wiremock.NhsMockServer
import uk.gov.justice.digital.hmpps.prisontonhs.integration.wiremock.OAuthMockServer
import uk.gov.justice.digital.hmpps.prisontonhs.integration.wiremock.PrisonMockServer
import uk.gov.justice.digital.hmpps.prisontonhs.integration.wiremock.PrisonRegisterMockServer
import uk.gov.justice.digital.hmpps.prisontonhs.services.JwtAuthHelper
import java.time.Duration

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class IntegrationTest {

  @SpyBean
  @Qualifier("awsSqsClient")
  internal lateinit var awsSqsClient: AmazonSQS

  @Autowired
  private lateinit var gson: Gson

  @Autowired
  internal lateinit var webTestClient: WebTestClient

  @Autowired
  internal lateinit var jwtHelper: JwtAuthHelper

  companion object {
    internal val prisonMockServer = PrisonMockServer()
    internal val oauthMockServer = OAuthMockServer()
    internal val prisonRegisterMockServer = PrisonRegisterMockServer()
    internal val nhsMockServer = NhsMockServer()

    @BeforeAll
    @JvmStatic
    fun startMocks() {
      prisonMockServer.start()
      prisonRegisterMockServer.start()
      nhsMockServer.start()
      oauthMockServer.start()
    }

    @AfterAll
    @JvmStatic
    fun stopMocks() {
      prisonMockServer.stop()
      prisonRegisterMockServer.stop()
      nhsMockServer.stop()
      oauthMockServer.stop()
    }
  }

  init {
    SecurityContextHolder.getContext().authentication = TestingAuthenticationToken("user", "pw")
    // Resolves an issue where Wiremock keeps previous sockets open from other tests causing connection resets
    System.setProperty("http.keepAlive", "false")
  }

  @BeforeEach
  fun resetStubs() {
    prisonMockServer.resetAll()
    prisonRegisterMockServer.resetAll()
    nhsMockServer.resetAll()
    oauthMockServer.resetAll()
    oauthMockServer.stubGrantToken()
  }

  internal fun Any.asJson() = gson.toJson(this)

  internal fun setAuthorisation(
    user: String = "prison-to-nhs-api-client",
    roles: List<String> = listOf()
  ): (HttpHeaders) -> Unit {
    val token = jwtHelper.createJwt(
      subject = user,
      scope = listOf("read"),
      expiryTime = Duration.ofHours(1L),
      roles = roles
    )
    return { it.set(HttpHeaders.AUTHORIZATION, "Bearer $token") }
  }
}
