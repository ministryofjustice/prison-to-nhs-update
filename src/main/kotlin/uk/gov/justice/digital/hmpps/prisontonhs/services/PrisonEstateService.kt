@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.prisontonhs.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Service
open class PrisonEstateService(
  @Qualifier("webClient") val webClient: WebClient,
  @Value("\${api.base.url.prison-estate}") val baseUri: String,
  @Value("\${api.prison-estate.timeout:5s}") val timeout: Duration
) {

  open fun getPrisonEstateByPrisonId(prisonId: String): PrisonEstate? {
    return webClient.get()
      .uri("$baseUri/prisons/id/$prisonId")
      .retrieve()
      .bodyToMono(PrisonEstate::class.java)
      .block(timeout)
  }

  open fun getPrisonEstateByGpPracticeCode(gpPracticeCode: String): PrisonEstate? {
    return webClient.get()
      .uri("$baseUri/prisons/gp-practice/$gpPracticeCode")
      .retrieve()
      .bodyToMono(PrisonEstate::class.java)
      .block(timeout)
  }
}

data class PrisonEstate(
  val prisonId: String,
  val name: String,
  val active: Boolean,
  val gpPracticeCode: String
)
