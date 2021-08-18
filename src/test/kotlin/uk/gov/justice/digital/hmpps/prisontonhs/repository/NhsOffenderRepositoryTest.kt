package uk.gov.justice.digital.hmpps.prisontonhs.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisontonhs.integration.IntegrationTest
import java.time.LocalDateTime

@Transactional
class NhsOffenderRepositoryTest : IntegrationTest() {

  @Autowired
  lateinit var repository: OffenderPatientRecordRepository

  @Test
  fun `should insert offender information`() {
    val offenderNomsId = "A1234AA"
    val now = LocalDateTime.now()

    val offenderInformation = OffenderPatientRecord(offenderNomsId, "{ \"id\": \"test\" }", now)

    val id = repository.save(offenderInformation).nomsId

    TestTransaction.flagForCommit()
    TestTransaction.end()

    val savedOffenderInformation = repository.findById(id).get()

    with(savedOffenderInformation) {
      assertThat(nomsId).isEqualTo(offenderNomsId)
      assertThat(patientRecord).isEqualTo("{ \"id\": \"test\" }")
      assertThat(updatedTimestamp).isEqualToIgnoringNanos(now)
    }
  }
}
