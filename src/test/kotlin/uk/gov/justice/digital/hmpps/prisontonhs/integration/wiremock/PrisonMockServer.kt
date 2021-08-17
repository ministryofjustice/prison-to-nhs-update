package uk.gov.justice.digital.hmpps.prisontonhs.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.anyUrl
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.google.gson.Gson
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.prisontonhs.services.PrisonerStatus
import java.net.HttpURLConnection.HTTP_OK

class PrisonExtension : BeforeAllCallback, AfterAllCallback, BeforeEachCallback {

  companion object {
    @JvmField
    val prisonApi = PrisonMockServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    prisonApi.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    prisonApi.resetRequests()
  }

  override fun afterAll(context: ExtensionContext) {
    prisonApi.stop()
  }
}

class PrisonMockServer : WireMockServer(WIREMOCK_PORT) {
  @Autowired
  private lateinit var gson: Gson

  companion object {
    private const val WIREMOCK_PORT = 8093
  }

  internal fun Any.asJson() = gson.toJson(this)

  fun prisonRequestCountFor(url: String) =
    PrisonExtension.prisonApi.findAll(WireMock.getRequestedFor(WireMock.urlEqualTo(url))).count()

  fun stubHealthPing(status: Int) {
    stubFor(
      get("/health/ping").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(if (status == 200) "pong" else "some error")
          .withStatus(status)
      )
    )
  }

  fun stubGetPrisoner(expectedPrisoner: PrisonerStatus) {
    stubFor(
      get(anyUrl()).willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(expectedPrisoner.asJson())
          .withStatus(HTTP_OK)
      )
    )
  }
}
