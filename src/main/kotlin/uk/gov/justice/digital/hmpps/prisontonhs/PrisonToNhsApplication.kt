package uk.gov.justice.digital.hmpps.prisontonhs

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer

@SpringBootApplication
@EnableResourceServer
open class PrisonToNhsUpdateApplication

fun main(args: Array<String>) {
  runApplication<PrisonToNhsUpdateApplication>(*args)
}
