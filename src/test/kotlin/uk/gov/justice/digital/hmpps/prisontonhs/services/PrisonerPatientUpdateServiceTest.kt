package uk.gov.justice.digital.hmpps.prisontonhs.services


import com.microsoft.applicationinsights.TelemetryClient
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever

import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import uk.gov.justice.digital.hmpps.prisontonhs.config.JsonConfig
import uk.gov.justice.digital.hmpps.prisontonhs.repository.OffenderPatientRecord
import uk.gov.justice.digital.hmpps.prisontonhs.repository.OffenderPatientRecordRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.EntityNotFoundException

class PrisonerPatientUpdateServiceTest {
    private val offenderService: OffenderService = mock()
    private val prisonEstateService: PrisonEstateService = mock()
    private val nhsReceiveService: NhsReceiveService = mock()
    private val offenderPatientRecordRepository: OffenderPatientRecordRepository = mock()
    private val telemetryClient: TelemetryClient = mock()

    private lateinit var service: PrisonerPatientUpdateService

    @BeforeEach
    fun before() {
        service = PrisonerPatientUpdateService(offenderService, prisonEstateService, nhsReceiveService, offenderPatientRecordRepository, telemetryClient, listOf("MDI", "LEI"), JsonConfig().gson())
    }

    @Test
    fun `will update NHS service for changed record`() {
        whenever(offenderService.getOffenderForBookingId(eq(12345L))).thenReturn(createOffenderBooking())
        whenever(offenderService.getOffender(anyString())).thenReturn(createPrisonerStatus())
        whenever(offenderPatientRecordRepository.findById(eq("AB1234D"))).thenReturn(Optional.of(createOffenderPatientRecord()))
        whenever(prisonEstateService.getPrisonEstateByPrisonId(anyString())).thenReturn(createPrisonEstate())

        service.offenderBookingChange(OffenderBookingChangedMessage(12345L))

        verify(nhsReceiveService).postNhsData(createNhsPrisoner(), ChangeType.AMENDMENT)
    }

    @Test
    fun `will not update NHS service for unchanged record`() {
        whenever(offenderService.getOffenderForBookingId(eq(12345L))).thenReturn(createOffenderBooking())
        whenever(offenderService.getOffender(anyString())).thenReturn(createPrisonerStatus())
        whenever(offenderPatientRecordRepository.findById(eq("AB1234D"))).thenReturn(Optional.of(updatedOffenderPatientRecord()))

        service.offenderBookingChange(OffenderBookingChangedMessage(12345L))

        verifyZeroInteractions(nhsReceiveService)
    }

    @Test
    fun `will update NHS service for small change of record`() {
        whenever(offenderService.getOffenderForBookingId(eq(12345L))).thenReturn(createOffenderBooking())
        whenever(offenderService.getOffender(anyString())).thenReturn(createPrisonerStatus())
        whenever(offenderPatientRecordRepository.findById(eq("AB1234D"))).thenReturn(Optional.of(updatedOffenderPatientRecordSmallChange()))
        whenever(prisonEstateService.getPrisonEstateByPrisonId(anyString())).thenReturn(createPrisonEstate())

        service.offenderBookingChange(OffenderBookingChangedMessage(12345L))

        verify(nhsReceiveService).postNhsData(createNhsPrisoner(), ChangeType.AMENDMENT)
    }

    @Test
    fun `will update NHS service offender change`() {
        whenever(offenderService.getOffenderForNomsId(anyString())).thenReturn(createOffenderBooking())
        whenever(offenderService.getOffender(anyString())).thenReturn(createPrisonerStatus())
        whenever(offenderPatientRecordRepository.findById(eq("AB1234D"))).thenReturn(Optional.of(updatedOffenderPatientRecordSmallChange()))
        whenever(prisonEstateService.getPrisonEstateByPrisonId(anyString())).thenReturn(createPrisonEstate())

        service.offenderChange(OffenderChangedMessage("AB1234D"))

        verify(nhsReceiveService).postNhsData(createNhsPrisoner(), ChangeType.AMENDMENT)
    }
    @Test
    fun `will not update NHS service prisons that are excluded`() {
        whenever(offenderService.getOffenderForBookingId(eq(12345L))).thenReturn(createOffenderBookingOtherPrison())

        service.offenderBookingChange(OffenderBookingChangedMessage(12345L))

        verifyZeroInteractions(nhsReceiveService)
    }

    @Test
    fun `will always sent to NHS for new record`() {
        whenever(offenderService.getOffenderForBookingId(eq(12345L))).thenReturn(createOffenderBooking())
        whenever(offenderService.getOffender(anyString())).thenReturn(createPrisonerStatus())
        whenever(offenderPatientRecordRepository.findById(eq("AB1234D"))).thenReturn(Optional.empty())
        whenever(prisonEstateService.getPrisonEstateByPrisonId(anyString())).thenReturn(createPrisonEstate())

        service.offenderBookingChange(OffenderBookingChangedMessage(12345L))

        verify(nhsReceiveService).postNhsData(createNhsPrisoner(), ChangeType.AMENDMENT)
    }

    @Test
    fun `will not send when no offender found`() {
        whenever(offenderService.getOffenderForBookingId(eq(12345L))).thenReturn(null)

        service.offenderBookingChange(OffenderBookingChangedMessage(12345L))

        verifyZeroInteractions(nhsReceiveService)
    }

    @Test
    fun `will skip when no gp code found for prison`() {
        whenever(offenderService.getOffenderForBookingId(eq(12345L))).thenReturn(createOffenderBooking())
        whenever(offenderService.getOffender(anyString())).thenReturn(createPrisonerStatus())
        whenever(offenderPatientRecordRepository.findById(eq("AB1234D"))).thenReturn(Optional.of(createOffenderPatientRecord()))
        whenever(prisonEstateService.getPrisonEstateByPrisonId(anyString())).thenReturn(null)

        assertThatExceptionOfType(EntityNotFoundException::class.java).isThrownBy {
            service.offenderBookingChange(OffenderBookingChangedMessage(12345L))
        }
        verifyZeroInteractions(nhsReceiveService)
    }

    @Test
    fun `will skip when no offender data found`() {
        whenever(offenderService.getOffenderForBookingId(eq(12345L))).thenReturn(createOffenderBooking())
        whenever(offenderService.getOffender(anyString())).thenReturn(null)

        service.offenderBookingChange(OffenderBookingChangedMessage(12345L))
        verifyZeroInteractions(nhsReceiveService)
    }

    @Test
    fun `will send a REGISTRATION notification to NHS for received offender into prison`() {
        whenever(offenderService.getOffender(anyString())).thenReturn(createPrisonerStatus())
        whenever(offenderPatientRecordRepository.findById(eq("AB1234D"))).thenReturn(Optional.of(createOffenderPatientRecord()))
        whenever(prisonEstateService.getPrisonEstateByPrisonId(anyString())).thenReturn(createPrisonEstate())

        service.externalMovement(ExternalPrisonerMovementMessage( 12345L, 1L, "AB1234D", "SCT", "MDI", "IN", "ADM"))

        verify(nhsReceiveService).postNhsData(createNhsPrisoner(), ChangeType.REGISTRATION)
    }

    @Test
    fun `will send a DEDUCTION notification to NHS for released offender from prison`() {
        whenever(offenderService.getOffender(anyString())).thenReturn(createPrisonerStatus())
        whenever(offenderPatientRecordRepository.findById(eq("AB1234D"))).thenReturn(Optional.of(createOffenderPatientRecord()))
        whenever(prisonEstateService.getPrisonEstateByPrisonId(anyString())).thenReturn(createPrisonEstate())

        service.externalMovement(ExternalPrisonerMovementMessage( 12345L, 1L, "AB1234D", "MDI", "OUT", "OUT", "REL"))

        verify(nhsReceiveService).postNhsData(createNhsPrisoner(), ChangeType.DEDUCTION)
    }

    @Test
    fun `will not send a notification for prisoner released from excluded prison`() {
        service.externalMovement(ExternalPrisonerMovementMessage( 12345L, 1L, "AB1234D", "PVI", "OUT", "OUT", "REL"))

        verifyZeroInteractions(nhsReceiveService)
    }

    @Test
    fun `will not send a notification for prisoner received into excluded prison`() {
        service.externalMovement(ExternalPrisonerMovementMessage( 12345L, 1L, "AB1234D", "SCT", "PVI", "IN", "ADM"))

        verifyZeroInteractions(nhsReceiveService)
    }

    @Test
    fun `will ignore other movement types`() {
        service.externalMovement(ExternalPrisonerMovementMessage( 12345L, 1L, "AB1234D", "MDI", "PVI", "OUT", "TRN"))

        verifyZeroInteractions(nhsReceiveService)
    }

    private fun createOffenderBooking() = OffenderBooking(
            offenderNo = "AB1234D",
            bookingId = 12345L,
            agencyId = "MDI"
    )

    private fun createOffenderBookingOtherPrison() = OffenderBooking(
            offenderNo = "AB1234D",
            bookingId = 12345L,
            agencyId = "PVI"
    )

    private fun createPrisonerStatus() = PrisonerStatus(
            nomsId = "AB1234D",
            establishmentCode = "MDI",
            bookingId = 12345L,
            givenName1 = "Patient 1",
            givenName2 = "John",
            lastName = "Smith",
            requestedName = "Bob",
            dateOfBirth = LocalDate.of(1978,1,2),
            gender = "Male",
            englishSpeaking = false,
            unitCode1 = "A",
            unitCode2 = "1",
            unitCode3 = "005",
            bookingBeginDate = LocalDate.of(2019,1,2),
            admissionDate = LocalDate.of(2020,1,2),
            releaseDate = LocalDate.of(2025,1,2),
            categoryCode = "C",
            communityStatus = "ACTIVE IN",
            legalStatus = "SENTENCED"
    )

    private fun createNhsPrisoner() = NhsPrisoner(
            nomsId = "AB1234D",
            establishmentCode = "MDI",
            gpPracticeCode = "V123223",
            givenName1 = "Patient 1",
            givenName2 = "John",
            lastName = "Smith",
            requestedName = "Bob",
            dateOfBirth = LocalDate.of(1978,1,2),
            gender = "Male",
            englishSpeaking = false,
            unitCode1 = "A",
            unitCode2 = "1",
            unitCode3 = "005",
            bookingBeginDate = LocalDate.of(2019,1,2),
            admissionDate = LocalDate.of(2020,1,2),
            releaseDate = LocalDate.of(2025,1,2),
            categoryCode = "C",
            communityStatus = "ACTIVE IN",
            legalStatus = "SENTENCED"
    )

    private fun createOffenderPatientRecord() = OffenderPatientRecord(
            nomsId = "AB1234D",
            patientRecord = "{\"nomsId\":\"AB1234D\"}",
            updatedTimestamp = LocalDateTime.now().minusDays(1)
    )

    private fun updatedOffenderPatientRecord() = OffenderPatientRecord(
            nomsId = "AB1234D",
            patientRecord = "{\n" +
                    "      \"nomsId\": \"AB1234D\",\n" +
                    "      \"establishmentCode\": \"MDI\",\n" +
                    "      \"bookingId\": 12345,\n" +
                    "      \"givenName1\": \"Patient 1\",\n" +
                    "      \"givenName2\": \"John\",\n" +
                    "      \"lastName\": \"Smith\",\n" +
                    "      \"requestedName\": \"Bob\",\n" +
                    "      \"dateOfBirth\": \"1978-01-02\",\n" +
                    "      \"gender\": \"Male\",\n" +
                    "      \"englishSpeaking\": false,\n" +
                    "      \"unitCode1\": \"A\",\n" +
                    "      \"unitCode2\": \"1\",\n" +
                    "      \"unitCode3\": \"005\",\n" +
                    "      \"bookingBeginDate\": \"2019-01-02\",\n" +
                    "      \"admissionDate\": \"2020-01-02\",\n" +
                    "      \"releaseDate\": \"2025-01-02\",\n" +
                    "      \"categoryCode\": \"C\",\n" +
                    "      \"communityStatus\": \"ACTIVE IN\",\n" +
                    "      \"legalStatus\": \"SENTENCED\"\n" +
                    "    }",
            updatedTimestamp = LocalDateTime.now().minusDays(1)
    )

    private fun updatedOffenderPatientRecordSmallChange() = OffenderPatientRecord(
            nomsId = "AB1234D",
            patientRecord = "{\n" +
                    "      \"nomsId\": \"AB1234D\",\n" +
                    "      \"establishmentCode\": \"MDI\",\n" +
                    "      \"bookingId\": 12345,\n" +
                    "      \"givenName1\": \"Patient 1\",\n" +
                    "      \"givenName2\": \"John\",\n" +
                    "      \"lastName\": \"Smith\",\n" +
                    "      \"requestedName\": \"Bob\",\n" +
                    "      \"dateOfBirth\": \"1978-01-02\",\n" +
                    "      \"gender\": \"Male\",\n" +
                    "      \"englishSpeaking\": false,\n" +
                    "      \"unitCode1\": \"A\",\n" +
                    "      \"unitCode2\": \"1\",\n" +
                    "      \"unitCode3\": \"003\",\n" +
                    "      \"bookingBeginDate\": \"2019-01-02\",\n" +
                    "      \"admissionDate\": \"2020-01-02\",\n" +
                    "      \"releaseDate\": \"2025-01-02\",\n" +
                    "      \"categoryCode\": \"C\",\n" +
                    "      \"communityStatus\": \"ACTIVE IN\",\n" +
                    "      \"legalStatus\": \"SENTENCED\"\n" +
                    "    }",
            updatedTimestamp = LocalDateTime.now().minusDays(1)
    )

    private fun createPrisonEstate() = PrisonEstate(
            prisonId = "MDI",
            name = "Moorlands",
            active = true,
            gpPracticeCode = "V123223"
    )


}