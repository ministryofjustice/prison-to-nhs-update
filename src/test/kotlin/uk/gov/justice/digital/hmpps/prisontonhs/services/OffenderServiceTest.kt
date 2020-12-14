package uk.gov.justice.digital.hmpps.prisontonhs.services

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.anyUrl
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.prisontonhs.integration.IntegrationTest
import java.net.HttpURLConnection.HTTP_OK
import java.time.LocalDate

class OffenderServiceTest : IntegrationTest() {
  @Autowired
  private lateinit var service: OffenderService

  @Test
  fun `test get offender calls rest endpoint`() {
    val expectedPrisoner = createPrisoner()

    prisonMockServer.stubFor(
      get(anyUrl()).willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(expectedPrisoner.asJson())
          .withStatus(HTTP_OK)
      )
    )

    val offender = service.getOffender("AB123D")

    assertThat(offender).isEqualTo(expectedPrisoner)
    prisonMockServer.verify(
      getRequestedFor(urlEqualTo("/api/prisoners/AB123D/full-status"))
        .withHeader("Authorization", equalTo("Bearer ABCDE"))
    )
  }

  private fun createPrisoner() = PrisonerStatus(
    nomsId = "AB123D",
    establishmentCode = "MDI",
    bookingId = 1L,
    givenName1 = "",
    givenName2 = "",
    lastName = "",
    requestedName = "",
    dateOfBirth = LocalDate.of(1970, 1, 1),
    gender = "",
    englishSpeaking = true,
    unitCode1 = "",
    unitCode2 = "",
    unitCode3 = "",
    bookingBeginDate = LocalDate.of(2019, 1, 1),
    admissionDate = LocalDate.of(2020, 1, 1),
    releaseDate = LocalDate.of(2022, 1, 1),
    categoryCode = "",
    communityStatus = "",
    legalStatus = ""
  )
}
