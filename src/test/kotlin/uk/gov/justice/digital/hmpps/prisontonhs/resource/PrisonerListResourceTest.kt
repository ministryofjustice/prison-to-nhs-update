package uk.gov.justice.digital.hmpps.prisontonhs.resource

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.prisontonhs.integration.IntegrationTest

class PrisonerListResourceTest : IntegrationTest() {

  @Test
  fun `access forbidden when no authority`() {

    webTestClient.get().uri("/prisoner-list/Y05537")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `access forbidden when no role`() {

    webTestClient.get().uri("/prisoner-list/Y05537")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `can retrieve a list of prisoners with correct role`() {
    webTestClient.get().uri("/prisoner-list/Y05537")
      .headers(setAuthorisation(roles = listOf("ROLE_PRISONER_ROLLCALL")))
      .exchange()
      .expectStatus().isOk
      .expectBody().json("successful_result_list".loadJson())
  }

  @Test
  fun `can retrieve a paged list of prisoners with correct role`() {
    webTestClient.get().uri("/prisoner-list/Y05537?page=1&size=2")
      .headers(setAuthorisation(roles = listOf("ROLE_PRISONER_ROLLCALL")))
      .exchange()
      .expectStatus().isOk
      .expectBody().json("successful_result_list_paged".loadJson())
  }

  private fun String.loadJson(): String =
    PrisonerListResourceTest::class.java.getResource("$this.json").readText()
}
