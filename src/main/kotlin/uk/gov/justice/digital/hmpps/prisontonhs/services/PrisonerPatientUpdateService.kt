package uk.gov.justice.digital.hmpps.prisontonhs.services

import com.google.common.collect.MapDifference
import com.google.common.collect.Maps
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.prisontonhs.repository.OffenderPatientRecord
import uk.gov.justice.digital.hmpps.prisontonhs.repository.OffenderPatientRecordRepository
import uk.gov.justice.digital.hmpps.prisontonhs.services.ChangeType.AMENDMENT
import uk.gov.justice.digital.hmpps.prisontonhs.services.ChangeType.REGISTRATION
import java.lang.reflect.Type
import java.time.LocalDateTime
import javax.persistence.EntityNotFoundException
import javax.transaction.Transactional


@Service
@Transactional
class PrisonerPatientUpdateService(
    private val offenderService: OffenderService,
    private val prisonEstateService: PrisonEstateService,
    private val nhsReceiveService: NhsReceiveService,
    private val offenderPatientRecordRepository: OffenderPatientRecordRepository,
    private val telemetryClient: TelemetryClient,
    @Value("\${prisontonhs.only.prisons}") private val allowedPrisons: List<String>,
    @Qualifier("gson") private val gson : Gson
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    fun externalMovement(externalMovement: ExternalPrisonerMovementMessage) {
        // only interested in ADM (Admissions) or REL (Releases)
        if (externalMovement.movementType in listOf("ADM", "REL")) {

            val changeType = if (externalMovement.movementType == "ADM") ChangeType.REGISTRATION else ChangeType.DEDUCTION
            val establishmentCode = if (changeType == REGISTRATION) externalMovement.toAgencyLocationId else externalMovement.fromAgencyLocationId

            // only support admission into an allowed prison or release from an allowed prison
            if ((establishmentCode in allowedPrisons)) {
                log.debug("Offender Movement {}", externalMovement)
                processPrisoner(externalMovement.offenderIdDisplay, changeType, establishmentCode)
            } else {
                log.debug("Skipping movement as not in allowed this [{}] prison yet {}", establishmentCode, externalMovement)
            }
        } else {
            log.debug("Ignored movement type {}", externalMovement)
        }

    }

    fun offenderBookingChange(message: OffenderBookingChangedMessage) {
        log.debug("Offender Booking Change [booking ID {}]", message.bookingId)

        offenderService.getOffenderForBookingId(message.bookingId)?.let {
            checkLocationAndProcessOffender(it)
        }
    }

    fun offenderChange(message: OffenderChangedMessage) {
        log.debug("Offender Change [Noms ID {}]", message.offenderIdDisplay)

        offenderService.getOffenderForNomsId(message.offenderIdDisplay)?.let {
            checkLocationAndProcessOffender(it)
        }
    }

    private fun checkLocationAndProcessOffender(booking: OffenderBooking) {
        // check if the offender is in an allowed prison
        if (booking.agencyId in allowedPrisons) {
            processPrisoner(booking.offenderNo, AMENDMENT, booking.agencyId)
        } else {
            log.debug("{} not in allowed list of prisons", booking.offenderNo)
        }
    }

    private fun processPrisoner(offenderNo: String, changeType: ChangeType, establishmentCode : String) {
        offenderService.getOffender(offenderNo)?.let { offender ->

            // check if changed
            val existingRecord = offenderPatientRecordRepository.findById(offenderNo)
            if (existingRecord.isPresent) {
                val jsonDiff = checkForDifferences(existingRecord.get().patientRecord, gson.toJson(offender))
                if (!jsonDiff.areEqual()) {
                    log.debug("Offender {} data changed", offender.nomsId)
                    val trackingAttributes = mapOf("nomsId" to offender.nomsId, "delta" to jsonDiff.toString())
                    telemetryClient.trackEvent("p2nhs-prisoner-change", trackingAttributes, null)

                    updateNhsSystem(offender, changeType, establishmentCode)
                } else {
                    log.debug("Offender {} data not changed", offender.nomsId)
                }
            } else {
                val trackingAttributes = mapOf("nomsId" to offender.nomsId)
                telemetryClient.trackEvent("p2nhs-prisoner-new", trackingAttributes, null)
                updateNhsSystem(offender, changeType, establishmentCode)
            }
        } ?: log.error("Offender not found {}", offenderNo)

    }

    private fun checkForDifferences(existingRecord: String, newRecord: String): MapDifference<String, Any> {
        val type: Type = object : TypeToken<Map<String?, Any?>?>() {}.type
        val leftMap: Map<String, Any> = gson.fromJson(existingRecord, type)
        val rightMap: Map<String, Any> = gson.fromJson(newRecord, type)
        return Maps.difference(leftMap, rightMap)
    }

    private fun updateNhsSystem(offender: PrisonerStatus, changeType: ChangeType, establishmentCode : String) : Boolean {
        log.debug("Saving patient record {}", offender.nomsId)
        offenderPatientRecordRepository.save(OffenderPatientRecord(offender.nomsId, gson.toJson(offender), LocalDateTime.now()))

        // look up the establishment code to get gp code
        prisonEstateService.getPrisonEstateByPrisonId(establishmentCode)?.let { prison ->

            // map the option to NhsPrisoner
            with(offender) {
                val nhsPrisoner = NhsPrisoner(nomsId, prison.gpPracticeCode, establishmentCode, givenName1, givenName2, lastName,
                        requestedName, dateOfBirth, gender, englishSpeaking, unitCode1, unitCode2, unitCode3,
                        bookingBeginDate, admissionDate, releaseDate, categoryCode, communityStatus, legalStatus)

                nhsReceiveService.postNhsData(nhsPrisoner, changeType)
            }
        } ?: throw EntityNotFoundException("Prison with prison id ${offender.establishmentCode} not found")

        return true;
    }

    private inline fun <reified T> fromJson(message: String): T {
        return gson.fromJson(message, T::class.java)
    }
}


