package uk.gov.justice.digital.hmpps.prisontonhs

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class PrisonToNhsUpdateApplication

fun main(args: Array<String>) {
  runApplication<PrisonToNhsUpdateApplication>(*args)
}
