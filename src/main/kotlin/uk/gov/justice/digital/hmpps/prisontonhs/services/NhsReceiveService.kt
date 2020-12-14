package uk.gov.justice.digital.hmpps.prisontonhs.services

import com.google.gson.Gson
import com.microsoft.applicationinsights.TelemetryClient
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
open class NhsReceiveService(
  @Qualifier("webClient") val webClient: WebClient,
  @Value("\${api.base.url.nhs}") val baseUri: String,
  @Value("\${api.nhs.timeout:30s}") val timeout: Duration,
  @Value("\${nhs.server.enabled:false}") val nhsServerEnabled: Boolean,
  @Qualifier("gson") val gson: Gson,
  val telemetryClient: TelemetryClient
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun postNhsData(nhsPrisonerData: NhsPrisoner, changeType: ChangeType): Boolean {

    val trackingAttributes = mapOf("nomsId" to nhsPrisonerData.nomsId, "change-type" to changeType.name)
    telemetryClient.trackEvent("p2nhs-send-to-nhs", trackingAttributes, null)

    if (nhsServerEnabled) {
      log.debug("Sending patient record {} to NHS", nhsPrisonerData.nomsId)
      webClient.post()
        .uri("$baseUri/patient-upsert")
        .header("change-type", changeType.name)
        .accept(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(gson.toJson(nhsPrisonerData)))
        .retrieve()
        .bodyToMono(String::class.java)
        .block(timeout)
      return true
    }
    log.warn("NHS Server sending not enabled")
    return false
  }
}
