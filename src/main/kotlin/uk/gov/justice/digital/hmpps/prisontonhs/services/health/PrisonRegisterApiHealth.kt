package uk.gov.justice.digital.hmpps.prisontonhs.services.health

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Component
class PrisonRegisterApiHealth(
  webClient: WebClient,
  @Value("\${api.base.url.prison-register}") baseUri: String,
  @Value("\${api.health-timeout:1s}") timeout: Duration
) :
  HealthCheck(webClient, baseUri, timeout)
