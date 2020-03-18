package uk.gov.justice.digital.hmpps.prisontonhs.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Service
open class NhsReceiveService(@Qualifier("webClient") val webClient: WebClient,
                             @Value("\${api.base.url.nhs}") val baseUri: String,
                             @Value("\${api.nhs.timeout:30s}") val timeout: Duration,
                             @Value("\${nhs.server.enabled:false}") val nhsServerEnabled: Boolean) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun postNhsData(nhsPrisonerData : NhsPrisoner, changeType: ChangeType) {
    log.debug("Sending patient record to NHS: $nhsPrisonerData")

    if (nhsServerEnabled) {
      webClient.post()
              .uri("$baseUri/patient-upsert")
              .header("change-type", changeType.name)
              .accept( MediaType.APPLICATION_JSON )
              .body(BodyInserters.fromValue(nhsPrisonerData))
              .retrieve()
              .bodyToMono(String::class.java)
              .block(timeout)
    } else {
      log.warn("NHS Server sending not enabled")
    }
  }

}

