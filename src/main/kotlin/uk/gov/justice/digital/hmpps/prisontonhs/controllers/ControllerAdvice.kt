package uk.gov.justice.digital.hmpps.prisontonhs.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.RestClientResponseException

@RestControllerAdvice(basePackages = ["uk.gov.justice.hmpps.prisontonhs.controllers"])
class ControllerAdvice {
    @ExceptionHandler(RestClientResponseException::class)
    fun handleRestClientResponseException(e: RestClientResponseException): ResponseEntity<ByteArray>? {
        return ResponseEntity
                .status(e.rawStatusCode)
                .body(e.responseBodyAsByteArray)
    }
}