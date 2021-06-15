package uk.gov.justice.digital.hmpps.prisontonhs

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.postgresql.util.PSQLException
import org.postgresql.util.PSQLState
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.prisontonhs.repository.OffenderPatientRecord
import uk.gov.justice.digital.hmpps.prisontonhs.repository.OffenderPatientRecordRepository
import java.time.LocalDateTime
import java.util.Optional

class MessageIntegrationDuplicateTest : QueueIntegrationTest() {
  @MockBean
  private lateinit var offenderPatientRecordRepository: OffenderPatientRecordRepository

  @Test
  fun `will consume a prison movement message in, update nhs`() {
    val message = "/messages/externalMovementIn.json".readResourceAsText()

    // wait until our queue has been purged
    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }

    whenever(offenderPatientRecordRepository.findById(any()))
      .thenReturn(Optional.empty())
      .thenReturn(
        Optional.of(
          OffenderPatientRecord(
            "A5089DY",
            """
        {
          "nomsId": "A5089DY",
          "establishmentCode": "MDI",
          "bookingId": 1200835,
          "givenName1": "Patient 1",
          "givenName2": "John",
          "lastName": "Smith",
          "requestedName": "Bob",
          "dateOfBirth": "1978-01-02",
          "gender": "Male",
          "englishSpeaking": false,
          "unitCode1": "A",
          "unitCode2": "1",
          "unitCode3": "005",
          "bookingBeginDate": "2019-01-02",
          "admissionDate": "2020-01-02",
          "releaseDate": "2025-01-02",
          "categoryCode": "C",
          "communityStatus": "ACTIVE IN",
          "legalStatus": "SENTENCED"
        }
            """.trimIndent(),
            LocalDateTime.MIN
          )
        )
      )
    whenever(offenderPatientRecordRepository.save(any())).thenAnswer {
      throw PSQLException("duplicate exception", PSQLState.DATA_ERROR)
    }

    awsSqsClient.sendMessage(sqsConfigProperties.queueName.queueUrl(), message)

    // First time round the listener will fail, but then will succeed on second attempt
    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }
    await untilCallTo { prisonRequestCountFor("/api/prisoners/A5089DY/full-status") } matches { it == 1 }
  }
}

private fun String.readResourceAsText(): String {
  return MessageIntegrationTest::class.java.getResource(this).readText()
}
