package uk.gov.justice.digital.hmpps.prisontonhs.services

import com.google.gson.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.prisontonhs.controllers.NhsPrisoner
import uk.gov.justice.digital.hmpps.prisontonhs.repository.OffenderPatientRecord
import uk.gov.justice.digital.hmpps.prisontonhs.repository.OffenderPatientRecordRepository
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.persistence.EntityNotFoundException


@Service
class PrisonerPatientUpdateService(
        private val offenderService: OffenderService,
        private val prisonEstateService: PrisonEstateService,
        private val nhsReceiveService: NhsReceiveService,
        private val offenderPatientRecordRepository: OffenderPatientRecordRepository,
        @Value("\${prisontonhs.only.prisons}") private val allowedPrisons: List<String>
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
        val gson: Gson = GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
                .create();
    }

    fun externalMovement(externalMovement: ExternalPrisonerMovementMessage) {
        // only interested in ADM (Admissions) or REL (Releases)
        if (externalMovement.movementType in listOf("ADM", "REL")) {

            val changeType = if (externalMovement.movementType === "ADM") ChangeType.REGISTRATION else ChangeType.DEDUCTION

            // only support admission into an allowed prison or release from an allowed prison
            if ((externalMovement.movementType === "ADM" && externalMovement.toAgencyLocationId in allowedPrisons)
                    || (externalMovement.movementType === "REL" && externalMovement.fromAgencyLocationId in allowedPrisons)) {
                log.debug("Offender Movement $externalMovement")
                processPrisoner(externalMovement.offenderIdDisplay, changeType)
            } else {
                log.debug("Skipping movement as not in allowed this prison yet $externalMovement")
            }
        } else {
            log.debug("Ignored movement type $externalMovement")
        }

    }

    fun offenderChange(message: OffenderChangedMessage) {
        log.debug("Offender Change {}", message)
        // check if the offender is in an allowed prison
        offenderService.getOffenderForBookingId(message.bookingId)?.let {
            if (it.agencyId in allowedPrisons) {
                processPrisoner(it.offenderNo, ChangeType.AMENDMENT)
            } else {
                log.debug("${it.offenderNo} not in allowed list of prisons")
            }
        }
    }

    private fun processPrisoner(offenderNo: String, changeType: ChangeType) {
        offenderService.getOffender(offenderNo)?.let { offender ->

            // check if changed
            val existingRecord = offenderPatientRecordRepository.findById(offenderNo)
            if (existingRecord.isPresent) {
                val prisonerData = fromJson<PrisonerStatus>(existingRecord.get().patientRecord)
                if (prisonerData != offender) {
                    log.debug("Offender record ${offender.nomsId} has changed")
                    updateNhsSystem(offender, changeType)
                } else {
                    log.debug("Offender ${offender.nomsId} data not changed")
                }
            } else {
                updateNhsSystem(offender, changeType)
            }
        } ?: log.error("Offender not found $offenderNo")

    }


    private fun updateNhsSystem(offender: PrisonerStatus, changeType: ChangeType) : Boolean {
        log.debug("Saving patient record {}", offender.nomsId)
        offenderPatientRecordRepository.save(OffenderPatientRecord(offender.nomsId, gson.toJson(offender), LocalDateTime.now()))

        // look up the establishment code to get gp code
        prisonEstateService.getPrisonEstateByPrisonId(offender.establishmentCode)?.let { prison ->

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

internal class LocalDateAdapter : JsonSerializer<LocalDate?>, JsonDeserializer<LocalDate?> {
    override fun serialize(src: LocalDate?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.format(DateTimeFormatter.ISO_LOCAL_DATE))
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalDate? {
        return LocalDate.parse(json?.asJsonPrimitive?.asString);
    }
}

