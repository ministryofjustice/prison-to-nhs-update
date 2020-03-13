package uk.gov.justice.digital.hmpps.prisontonhs.controllers

import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.prisontonhs.services.OffenderService
import uk.gov.justice.digital.hmpps.prisontonhs.services.PrisonEstateService
import java.time.LocalDate

@RestController
@Validated
@RequestMapping("/prisoner-list", produces = [MediaType.APPLICATION_JSON_VALUE])
class PrisonerListResource(private val prisonEstateService: PrisonEstateService,
                           private val offenderService: OffenderService) {

    @GetMapping("/{gpPrescriberCode}")
    fun getPrisonersByGpPrescriberCode(@PathVariable gpPrescriberCode : String) : List<NhsPrisoner> {
        val prisonDetail = prisonEstateService.getPrisonEstateByGpPracticeCode(gpPrescriberCode)!!

        val offendersInEstablishment = offenderService.getOffendersInEstablishment(prisonDetail.prisonId)

        return offendersInEstablishment
                .map { o -> (
                        with(o) {
                            NhsPrisoner(nomsId, establishmentCode, gpPrescriberCode, givenName1, givenName2, lastName,
                            requestedName, dateOfBirth, gender, englishSpeaking, unitCode1, unitCode2, unitCode3,
                            bookingBeginDate, admissionDate,releaseDate, categoryCode,communityStatus, legalStatus)
                        }
                )}

    }
}

data class NhsPrisoner (
        val nomsId: String,
        val establishmentCode: String,
        val gpPrescriberCode: String,
        val givenName1: String,
        val givenName2: String?,
        val lastName: String,
        val requestedName: String?,
        val dateOfBirth: LocalDate,
        val gender: String,
        val englishSpeaking: String,
        val unitCode1: String,
        val unitCode2: String?,
        val unitCode3: String?,
        val bookingBeginDate: LocalDate,
        val admissionDate: LocalDate?,
        val releaseDate: LocalDate?,
        val categoryCode: String?,
        val communityStatus: String,
        val legalStatus: String
)
