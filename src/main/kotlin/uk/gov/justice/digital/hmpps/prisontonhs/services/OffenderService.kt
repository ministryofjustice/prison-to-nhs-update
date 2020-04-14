@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.prisontonhs.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration
import java.time.LocalDate

@Service
class OffenderService(val prisonWebClient: WebClient,
                      @Value("\${api.offender.timeout:5s}") val offenderTimeout: Duration,
                      @Value("\${api.nomis.timeout:90s}") val offenderListTimeout: Duration) {

  private val prisonerPageType = object : ParameterizedTypeReference<RestResponsePage<PrisonerStatus>>() {}

  fun getOffenderForBookingId(bookingId: Long): OffenderBooking? {
    return prisonWebClient.get()
        .uri("/api/bookings/$bookingId")
        .retrieve()
        .bodyToMono(OffenderBooking::class.java)
        .block(offenderTimeout)
  }

  fun getOffenderForNomsId(nomsId: String): OffenderBooking? {
    return prisonWebClient.get()
        .uri("/api/bookings/offenderNo/$nomsId")
        .retrieve()
        .bodyToMono(OffenderBooking::class.java)
        .block(offenderTimeout)
  }

  fun getOffender(offenderNo: String): PrisonerStatus? {
    return prisonWebClient
        .get()
        .uri("/api/prisoners/$offenderNo/full-status")
        .retrieve()
        .bodyToMono(PrisonerStatus::class.java)
        .block(offenderTimeout)
  }

  fun getOffendersInEstablishment(establishmentCode: String, page : Int, size : Int): Page<PrisonerStatus>? {
    return prisonWebClient
        .get()
        .uri("/api/prisoners/by-establishment/${establishmentCode}?page=${page}&size=${size}&sort=nomsId")
        .retrieve()
        .bodyToMono(prisonerPageType)
        .block(offenderListTimeout)
  }

}

data class PrisonerStatus(
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
    val unitCode1: String?,
    val unitCode2: String?,
    val unitCode3: String?,
    val bookingBeginDate: LocalDate?,
    val admissionDate: LocalDate?,
    val releaseDate: LocalDate?,
    val categoryCode: String?,
    val communityStatus: String,
    val legalStatus: String?
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