package uk.gov.justice.digital.hmpps.prisontonhs.services

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.prisontonhs.controllers.NhsPrisoner
import uk.gov.justice.digital.hmpps.prisontonhs.repository.OffenderPatientRecord
import uk.gov.justice.digital.hmpps.prisontonhs.repository.OffenderPatientRecordRepository
import java.time.LocalDateTime
import javax.persistence.EntityNotFoundException


@Service
open class PrisonerPatientUpdateService(
        private val offenderService: OffenderService,
        private val prisonEstateService: PrisonEstateService,
        private val nhsReceiveService: NhsReceiveService,
        private val offenderPatientRecordRepository: OffenderPatientRecordRepository,
        @Value("\${prisontonhs.only.prisons}") private val allowedPrisons: List<String>
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
        val gson: Gson = GsonBuilder().create()
    }

    fun externalMovement(externalMovement: ExternalPrisonerMovementMessage) {

        if (externalMovement.movementType in listOf("ADM", "REL")) {

            val changeType = if (externalMovement.movementType === "ADM") ChangeType.REGISTRATION else ChangeType.DEDUCTION

            if ((externalMovement.movementType === "ADM" && externalMovement.toAgencyLocationId in allowedPrisons)
                    || (externalMovement.movementType === "REL" && externalMovement.fromAgencyLocationId in allowedPrisons)) {
                log.debug("Offender Movement {}", externalMovement)
                processPrisoner(externalMovement.offenderIdDisplay, changeType)
            } else {

                log.debug("Skipping movement {} as not in allowed prisons yet", externalMovement)
            }
        }

    }

    fun offenderChange(message: OffenderChangedMessage) {
        log.debug("Offender Change {}", message)
        // check if the offender is in an allowed prison
        val let = offenderService.getOffenderForBookingId(message.bookingId)?.let {
            if (it.agencyId in allowedPrisons) {
                processPrisoner(it.offenderNo, ChangeType.AMENDMENT)
            } else {
                log.debug("$it.offenderNo not in allowed list of prisons")
            }
        }
    }

    private fun processPrisoner(offenderNo: String, changeType: ChangeType) {
        offenderService.getOffender(offenderNo)?.let { offender ->

        // check if changed
        offenderPatientRecordRepository.findById(offenderNo)
            .map {
                val prisonerData = fromJson<PrisonerStatus>(it.patientRecord)
                if (prisonerData != offender) {
                    updateNhsSystem(offender, changeType)
                } else {
                    log.debug("offender {} data not changed", offender.nomsId)
                }
            } ?: updateNhsSystem(offender, changeType)
        } ?: log.error("Offender not found {}", offenderNo)

    }


    private fun updateNhsSystem(offender: PrisonerStatus, changeType: ChangeType) {
        offenderPatientRecordRepository.save(OffenderPatientRecord(offender.nomsId, gson.toJson(offender), LocalDateTime.now()))

        // look up the establishment code to get gp code
        prisonEstateService.getPrisonEstateByPrisonId(offender.establishmentCode)?.let { prison ->

            // map the option to NhsPrisoner
            with(offender) {
                val nhsPrisoner = NhsPrisoner(nomsId, establishmentCode, prison.gpPracticeCode, givenName1, givenName2, lastName,
                        requestedName, dateOfBirth, gender, englishSpeaking, unitCode1, unitCode2, unitCode3,
                        bookingBeginDate, admissionDate, releaseDate, categoryCode, communityStatus, legalStatus)

                nhsReceiveService.postNhsData(nhsPrisoner, changeType)
            }
        } ?: throw EntityNotFoundException("Prison with prison id $offender.establishmentCode not found")
    }

    private inline fun <reified T> fromJson(message: String): T {
        return gson.fromJson(message, T::class.java)
    }
}



