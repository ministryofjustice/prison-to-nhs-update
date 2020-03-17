package uk.gov.justice.digital.hmpps.prisontonhs

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class PrisonToNhsUpdateApplication

fun main(args: Array<String>) {
  runApplication<PrisonToNhsUpdateApplication>(*args)
}
