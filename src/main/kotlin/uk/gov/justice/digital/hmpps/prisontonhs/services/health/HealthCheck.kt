package uk.gov.justice.digital.hmpps.prisontonhs.services.health

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

abstract class HealthCheck(private val webClient: WebClient,
                           private val baseUri: String,
                           private val timeout: Duration
) : HealthIndicator {

    override fun health(): Health? {
        return try {
            val uri = "$baseUri/health/ping"
            val response = webClient.get()
                    .uri(uri)
                    .exchange()
                    .block(timeout)
            Health.up().withDetail("HttpStatus", response.statusCode()).build()
        } catch (e: Exception) {
            Health.down(e).build()
        }
    }

}

