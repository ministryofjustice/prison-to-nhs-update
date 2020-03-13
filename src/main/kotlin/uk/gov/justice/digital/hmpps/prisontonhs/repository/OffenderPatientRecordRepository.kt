package uk.gov.justice.digital.hmpps.prisontonhs.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.Id

@Repository
interface OffenderPatientRecordRepository : CrudRepository<OffenderPatientRecord, String>

@Entity
data class OffenderPatientRecord(
        @Id
        val nomsId: String,
        val patientRecord: String,
        val updatedTimestamp: LocalDateTime
)