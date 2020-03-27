package uk.gov.justice.digital.hmpps.prisontonhs

import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test

class MessageIntegrationTest : QueueIntegrationTest() {


  @Test
  fun `will consume a prison movement message, update nhs`() {
    val message = "/messages/externalMovement.json".readResourceAsText()

    // wait until our queue has been purged
    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }

    awsSqsClient.sendMessage(queueUrl, message)

    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }
    await untilCallTo { prisonRequestCountFor("/api/bookings/1200835") } matches { it == 1 }
    await untilCallTo { prisonRequestCountFor("/api/prisoners/A5089DY/full-status") } matches { it == 1 }
    await untilCallTo { estateRequestCountFor("/prisons/gp-practice/Y1234AD") } matches { it == 1 }
    await untilCallTo { nhsPostCountFor("/patient-upsert") } matches { it == 1 }
  }

}

private fun String.readResourceAsText(): String {
  return MessageIntegrationTest::class.java.getResource(this).readText()
}