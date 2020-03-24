package uk.gov.justice.digital.hmpps.prisontonhs.services

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.util.*


internal class RestResponsePage<T> : PageImpl<T> {
  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  constructor(content: List<T>,
              number: Int,
              size: Int,
              totalElements: Long?,
              pageable: JsonNode?,
              last: Boolean,
              totalPages: Int,
              sort: JsonNode?,
              first: Boolean,
              numberOfElements: Int) : super(content, PageRequest.of(number, size), totalElements!!)

  constructor(content: List<T>, pageable: Pageable, total: Long) : super(content, pageable, total)
  constructor(content: List<T>) : super(content)
  constructor() : super(ArrayList<T>())

}