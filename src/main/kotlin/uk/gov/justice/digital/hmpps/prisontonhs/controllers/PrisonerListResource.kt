package uk.gov.justice.digital.hmpps.prisontonhs.controllers

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import io.swagger.annotations.Authorization
import org.springframework.data.domain.Page
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.prisontonhs.services.EstablishmentService
import uk.gov.justice.digital.hmpps.prisontonhs.services.NhsPrisoner
import javax.validation.constraints.Size

@RestController
@Validated
@RequestMapping("/prisoner-list", produces = [MediaType.APPLICATION_JSON_VALUE])
class PrisonerListResource(private val establishmentService: EstablishmentService) {

    @GetMapping("/{gpPracticeCode}")
    @ApiOperation(value = "Retrieve all offenders from a specified GP Code, sorted by NOMS ID", authorizations = [Authorization("SYSTEM_USER")])
    @ApiResponses(value = [
        ApiResponse(code = 400, message = "Bad request.  Wrong format for GP Code.", response = ErrorResponse::class),
        ApiResponse(code = 404, message = "GP Practice Code not found.", response = ErrorResponse::class)
    ])
    @PreAuthorize("hasAnyRole('SYSTEM_USER')")
    fun getPrisonersByGpPracticeCode(@ApiParam("GP Practice Code", example = "Y05537") @PathVariable @Size(max = 6) gpPracticeCode: String,
                                     @ApiParam("Page Number offset (detault 0)", example = "0", defaultValue = "0") @RequestParam(value = "page", required = false, defaultValue = "0") page : Integer,
                                     @ApiParam("Number of records returned", example = "10", defaultValue = "10") @RequestParam(value = "size", required = false, defaultValue = "10") size : Integer)
        : Page<NhsPrisoner> {
        return establishmentService.getPrisonersByGpPracticeCode(gpPracticeCode, page, size)
    }

}

