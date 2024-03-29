package uk.gov.justice.digital.hmpps.prisontonhs.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class NhsExtension : BeforeAllCallback, AfterAllCallback, BeforeEachCallback {
  companion object {
    @JvmField
    val nhsApi = NhsMockServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    nhsApi.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    nhsApi.resetRequests()
  }

  override fun afterAll(context: ExtensionContext) {
    nhsApi.stop()
  }
}

class NhsMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 7080
  }

  fun nhsPostCountFor(url: String) = NhsExtension.nhsApi.findAll(WireMock.postRequestedFor(WireMock.urlEqualTo(url))).count()

  fun stubHealthPing(status: Int) {
    stubFor(
      get("/auth/health/ping").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(if (status == 200) "pong" else "some error")
          .withStatus(status)
      )
    )
  }
}
