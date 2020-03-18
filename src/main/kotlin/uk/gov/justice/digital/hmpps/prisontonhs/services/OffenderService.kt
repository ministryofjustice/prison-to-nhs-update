@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.prisontonhs.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.domain.Page
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration
import java.time.LocalDate

@Service
open class OffenderService(@Qualifier("oauth2WebClient") val webClient: WebClient,
                           @Value("\${api.base.url.nomis}") val baseUri: String,
                           @Value("\${api.offender.timeout:5s}") val offenderTimeout: Duration,
                           @Value("\${api.nomis.timeout:90s}") val offenderListTimeout: Duration) {

  private val prisonerPageType = object : ParameterizedTypeReference<Page<PrisonerStatus>>() {}

  open fun getOffenderForBookingId(bookingId : Long) : OffenderBooking? {
    return webClient.get()
            .uri("$baseUri/api/bookings/$bookingId")
            .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId("nomis-api"))
            .retrieve()
            .bodyToMono(OffenderBooking::class.java)
            .block(offenderTimeout)
  }

  fun getOffender(offenderNo: String): PrisonerStatus? {
    return webClient
            .get()
            .uri("$baseUri/api/prisoners/$offenderNo/full-status")
            .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId("nomis-api"))
            .retrieve()
            .bodyToMono(PrisonerStatus::class.java)
            .block(offenderTimeout);
  }

  open fun getOffendersInEstablishment(establishmentCode: String): List<PrisonerStatus>? {
    val response = webClient
            .get()
            .uri("$baseUri/api/prisoners/by-establishment/${establishmentCode}?size=2000")
            .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId("nomis-api"))
            .retrieve()
            .bodyToMono(prisonerPageType)
            .block(offenderListTimeout)

    return response?.content;
  }

}

data class PrisonerStatus (
        val nomsId: String,
        val establishmentCode: String,
        val bookingId: Long,
        val givenName1: String,
        val givenName2: String?,
        val lastName: String,
        val requestedName: String?,
        val dateOfBirth: LocalDate,
        val gender: String,
        val englishSpeaking: Boolean,
        val unitCode1: String,
        val unitCode2: String?,
        val unitCode3: String?,
        val bookingBeginDate: LocalDate,
        val admissionDate: LocalDate?,
        val releaseDate: LocalDate?,
        val categoryCode: String?,
        val communityStatus: String,
        val legalStatus: String
)

data class Offender(
        val offenderNo: String,
        val offenderId: Long
)

data class OffenderBooking(
        val offenderNo: String,
        val bookingId: Long,
        val agencyId: String
)