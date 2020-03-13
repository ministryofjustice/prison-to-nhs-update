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

  open fun externalMovement(message: ExternalPrisonerMovementMessage) {
    log.debug("Offender Movement {}", message)

      val (offenderNo, _) = offenderService.getOffenderForBookingId(message.bookingId)!!

    processPrisoner(offenderNo)

  }

  open fun offenderChange(message: OffenderChangedMessage) {
    log.debug("Offender Change {}", message)
    val (offenderNo, _) = offenderService.getOffenderForBookingId(message.bookingId)!!

    processPrisoner(offenderNo)

  }

  private fun processPrisoner(offenderNo: String) {
    val offender = offenderService.getOffender(offenderNo)

    val patientRecord = gson.toJson(offender)

    // check if changed
    val savedPatientData = offenderPatientRecordRepository.findById(offenderNo)
    if (savedPatientData.isPresent) {
      if (savedPatientData.get().patientRecord == patientRecord) {
        return
      }
    }

    // store the change
    offenderPatientRecordRepository.save(OffenderPatientRecord(offenderNo, patientRecord, LocalDateTime.now()))

    // look up the establishment code to get gp code
    val (_, _, _, gpPracticeCode) = prisonEstateService.getPrisonEstateByPrisonId(offender.establishmentCode)!!

    // map the option to NhsPrisoner
    val nhsPrisoner = with(offender) {
      val nhsPrisoner = NhsPrisoner(nomsId, establishmentCode, gpPracticeCode, givenName1, givenName2, lastName,
              requestedName, dateOfBirth, gender, englishSpeaking, unitCode1, unitCode2, unitCode3,
              bookingBeginDate, admissionDate, releaseDate, categoryCode, communityStatus, legalStatus)
      nhsPrisoner
    }

    // post data to TTP system
    nhsReceiveService.postNhsData(nhsPrisoner)
  }
}



