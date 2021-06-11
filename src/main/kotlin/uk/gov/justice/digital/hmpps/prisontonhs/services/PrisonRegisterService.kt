@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.prisontonhs.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Service
open class PrisonRegisterService(
  @Qualifier("webClient") val webClient: WebClient,
  @Value("\${api.base.url.prison-register}") val baseUri: String,
  @Value("\${api.prison-register.timeout:5s}") val timeout: Duration
) {

  open fun getPrisonRegisterByPrisonId(prisonId: String): PrisonRegister? {
    return webClient.get()
      .uri("$baseUri/gp/prison/$prisonId")
      .retrieve()
      .bodyToMono(PrisonRegister::class.java)
      .block(timeout)
  }

  open fun getPrisonRegisterByGpPracticeCode(gpPracticeCode: String): PrisonRegister? {
    return webClient.get()
      .uri("$baseUri/gp/practice/$gpPracticeCode")
      .retrieve()
      .bodyToMono(PrisonRegister::class.java)
      .block(timeout)
  }
}

data class PrisonRegister(
  val prisonId: String,
  val prisonName: String,
  val active: Boolean,
  val gpPracticeCode: String
)
