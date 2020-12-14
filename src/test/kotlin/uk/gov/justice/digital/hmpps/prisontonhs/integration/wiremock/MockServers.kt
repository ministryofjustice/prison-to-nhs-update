package uk.gov.justice.digital.hmpps.prisontonhs.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.google.gson.GsonBuilder

class PrisonMockServer : WireMockServer(8093)

class PrisonEstateMockServer : WireMockServer(7070)

class NhsMockServer : WireMockServer(7080)

class OAuthMockServer : WireMockServer(8090) {
  private val gson = GsonBuilder().create()

  fun stubGrantToken() {
    stubFor(
      WireMock.post(WireMock.urlEqualTo("/auth/oauth/token"))
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(gson.toJson(mapOf("access_token" to "ABCDE", "token_type" to "bearer")))
        )
    )
  }
}
