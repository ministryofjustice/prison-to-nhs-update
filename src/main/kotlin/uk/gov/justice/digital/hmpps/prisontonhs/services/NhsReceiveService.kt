package uk.gov.justice.digital.hmpps.prisontonhs.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.prisontonhs.controllers.NhsPrisoner
import java.time.Duration

@Service
open class NhsReceiveService(@Qualifier("webClient") val webClient: WebClient,
                             @Value("\${api.base.url.nhs}") val baseUri: String) {

  private val timeout: Duration = Duration.ofSeconds(30)

  open fun postNhsData(nhsPrisonerData : NhsPrisoner, changeType: ChangeType) {
    webClient.post()
            .uri("$baseUri/patient-upsert")
            .header("change-type", changeType.name)
            .accept( MediaType.APPLICATION_JSON )
            .body(BodyInserters.fromValue(nhsPrisonerData))
            .retrieve()
            .bodyToMono(String::class.java)
            .block(timeout)
  }
}
