package uk.gov.justice.digital.hmpps.prisontonhs

import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test

class MessageIntegrationTest : QueueIntegrationTest() {


  @Test
  fun `will consume a prison movement message in, update nhs`() {
    val message = "/messages/externalMovementIn.json".readResourceAsText()

    // wait until our queue has been purged
    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }

    awsSqsClient.sendMessage(queueUrl, message)

    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }
    await untilCallTo { prisonRequestCountFor("/api/prisoners/A5089DY/full-status") } matches { it == 1 }
    await untilCallTo { estateRequestCountFor("/prisons/id/MDI") } matches { it == 1 }
    await untilCallTo { nhsPostCountFor("/patient-upsert") } matches { it == 1 }
  }

  @Test
  fun `will consume a prison movement message out, update nhs`() {
    val message = "/messages/externalMovementOut.json".readResourceAsText()

    // wait until our queue has been purged
    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }

    awsSqsClient.sendMessage(queueUrl, message)

    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }
    await untilCallTo { prisonRequestCountFor("/api/prisoners/A5089EY/full-status") } matches { it == 1 }
    await untilCallTo { estateRequestCountFor("/prisons/id/MDI") } matches { it == 1 }
    await untilCallTo { nhsPostCountFor("/patient-upsert") } matches { it == 1 }
  }

  @Test
  fun `will consume a prisoner imprisonment status change and update nhs`() {
    val message = "/messages/imprisonmentStatusChanged.json".readResourceAsText()

    // wait until our queue has been purged
    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }

    awsSqsClient.sendMessage(queueUrl, message)

    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }
    await untilCallTo { prisonRequestCountFor("/api/bookings/1900835") } matches { it == 1 }
    await untilCallTo { prisonRequestCountFor("/api/prisoners/A6089DY/full-status") } matches { it == 1 }
    await untilCallTo { estateRequestCountFor("/prisons/id/MDI") } matches { it == 1 }
    await untilCallTo { nhsPostCountFor("/patient-upsert") } matches { it == 1 }
  }

  @Test
  fun `will consume a offender update change and update nhs`() {
    val message = "/messages/offenderDetailsChanged.json".readResourceAsText()

    // wait until our queue has been purged
    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }

    awsSqsClient.sendMessage(queueUrl, message)

    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }
    await untilCallTo { prisonRequestCountFor("/api/bookings/offenderNo/A7089EY") } matches { it == 1 }
    await untilCallTo { prisonRequestCountFor("/api/prisoners/A7089EY/full-status") } matches { it == 1 }
    await untilCallTo { estateRequestCountFor("/prisons/id/MDI") } matches { it == 1 }
    await untilCallTo { nhsPostCountFor("/patient-upsert") } matches { it == 1 }
  }
}

private fun String.readResourceAsText(): String {
  return MessageIntegrationTest::class.java.getResource(this).readText()
}