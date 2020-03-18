package uk.gov.justice.digital.hmpps.prisontonhs.controllers

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.prisontonhs.services.EstablishmentService
import uk.gov.justice.digital.hmpps.prisontonhs.services.NhsPrisoner
import javax.validation.constraints.Size

@RestController
@Validated
@RequestMapping("/prisoner-list", produces = [MediaType.APPLICATION_JSON_VALUE])
class PrisonerListResource(private val establishmentService: EstablishmentService) {

    @GetMapping("/{gpPracticeCode}")
    @ApiOperation("Retrieve all offenders from a specified GP Code")
    @ApiResponses(value = [
        ApiResponse(code = 400, message = "Bad request.  Wrong format for GP Code.", response = ErrorResponse::class),
        ApiResponse(code = 404, message = "GP Practice Code not found.", response = ErrorResponse::class)
    ])
    fun getPrisonersByGpPracticeCode(@ApiParam("GP Practice Code", example = "Y05537") @PathVariable @Size(max = 6) gpPracticeCode: String): List<NhsPrisoner> {
        return establishmentService.getPrisonersByGpPracticeCode(gpPracticeCode)
    }

}

