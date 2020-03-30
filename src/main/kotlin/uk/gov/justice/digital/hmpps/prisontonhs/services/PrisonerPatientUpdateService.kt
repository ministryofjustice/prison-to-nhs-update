package uk.gov.justice.digital.hmpps.prisontonhs.services

import com.google.common.collect.MapDifference
import com.google.common.collect.Maps
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.prisontonhs.repository.OffenderPatientRecord
import uk.gov.justice.digital.hmpps.prisontonhs.repository.OffenderPatientRecordRepository
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

            // only support admission into an allowed prison or release from an allowed prison
            if ((externalMovement.movementType == "ADM" && externalMovement.toAgencyLocationId in allowedPrisons)
                    || (externalMovement.movementType == "REL" && externalMovement.fromAgencyLocationId in allowedPrisons)) {
                log.debug("Offender Movement $externalMovement")
                val establishmentCode = if (externalMovement.movementType == "ADM") externalMovement.toAgencyLocationId else externalMovement.fromAgencyLocationId
                processPrisoner(externalMovement.offenderIdDisplay, changeType, establishmentCode)
            } else {
                log.debug("Skipping movement as not in allowed this prison yet $externalMovement")
            }
        } else {
            log.debug("Ignored movement type $externalMovement")
        }

    }

    fun offenderBookingChange(message: OffenderBookingChangedMessage) {
        log.debug("Offender Change [booking ID ${message.bookingId}]")
        // check if the offender is in an allowed prison
        offenderService.getOffenderForBookingId(message.bookingId)?.let {
            if (it.agencyId in allowedPrisons) {
                processPrisoner(it.offenderNo, ChangeType.AMENDMENT, it.agencyId)
            } else {
                log.debug("${it.offenderNo} not in allowed list of prisons")
            }
        }
    }

    fun offenderChange(message: OffenderChangedMessage) {
        log.debug("Offender Change [Noms ID ${message.offenderIdDisplay}]")

        offenderService.getOffender(message.offenderIdDisplay)?.let {
            // check if the offender is in an allowed prison
            if (it.establishmentCode in allowedPrisons) {
                processPrisoner(it.nomsId, ChangeType.AMENDMENT, it.establishmentCode)
            } else {
                log.debug("${it.nomsId} not in allowed list of prisons")
            }
        }
    }

    private fun processPrisoner(offenderNo: String, changeType: ChangeType, establishmentCode : String) {
        offenderService.getOffender(offenderNo)?.let { offender ->

            // check if changed
            val existingRecord = offenderPatientRecordRepository.findById(offenderNo)
            if (existingRecord.isPresent) {
                val jsonDiff = checkForDifferences(existingRecord.get().patientRecord, gson.toJson(offender))
                if (!jsonDiff.areEqual()) {
                    log.debug("Offender record ${offender.nomsId} has changed: $jsonDiff")
                    updateNhsSystem(offender, changeType, establishmentCode)
                } else {
                    log.debug("Offender ${offender.nomsId} data not changed")
                }
            } else {
                updateNhsSystem(offender, changeType, establishmentCode)
            }
        } ?: log.error("Offender not found $offenderNo")

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


