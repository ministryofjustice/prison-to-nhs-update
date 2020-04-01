package uk.gov.justice.digital.hmpps.prisontonhs.services

import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import javax.persistence.EntityNotFoundException

@Service
class EstablishmentService(private val prisonEstateService: PrisonEstateService,
                           private val offenderService: OffenderService) {

    fun getPrisonersByGpPracticeCode(gpPracticeCode: String, page : Int, size : Int): Page<NhsPrisoner> {
        return prisonEstateService.getPrisonEstateByGpPracticeCode(gpPracticeCode)?.let {
            offenderService.getOffendersInEstablishment(it.prisonId, page, size)?.let { offenders ->
                offenders.map { offender ->
                    (
                            with(offender) {
                                NhsPrisoner(nomsId, gpPracticeCode, establishmentCode, givenName1, givenName2, lastName,
                                        requestedName, dateOfBirth, gender, englishSpeaking, unitCode1, unitCode2, unitCode3,
                                        bookingBeginDate, admissionDate, releaseDate, categoryCode, communityStatus, legalStatus)
                            }
                            )
                }
            }
        } ?: throw EntityNotFoundException("Prison with gp practice $gpPracticeCode not found")
    }
}
