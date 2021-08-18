package uk.gov.justice.digital.hmpps.prisontonhs

import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.prisontonhs.integration.wiremock.NhsExtension.Companion.nhsApi
import uk.gov.justice.digital.hmpps.prisontonhs.integration.wiremock.PrisonExtension.Companion.prisonApi
import uk.gov.justice.digital.hmpps.prisontonhs.integration.wiremock.PrisonRegisterExtension.Companion.prisonRegisterApi

class MessageIntegrationTest : QueueIntegrationTest() {

//  @MockBean
//  private lateinit var telemetryClient: TelemetryClient

  @Test
  fun `will consume a prison movement message in, update nhs`() {
    val message = "/messages/externalMovementIn.json".readResourceAsText()

    // wait until our queue has been purged
    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }

    awsSqsClient.sendMessage(queueUrl, message)

    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }
    await untilCallTo { prisonApi.prisonRequestCountFor("/api/prisoners/A5089DY/full-status") } matches { it == 1 }
    await untilCallTo { prisonRegisterApi.registerRequestCountFor("/gp/prison/MDI") } matches { it == 1 }
    await untilCallTo { nhsApi.nhsPostCountFor("/patient-upsert") } matches { it == 1 }

//    verify(telemetryClient).trackEvent(eq("p2nhs-prisoner-new"), any(), isNull())
//    verify(telemetryClient).trackEvent(eq("p2nhs-send-to-nhs"), eq(mapOf("nomsId" to "A5089DY", "change-type" to ChangeType.REGISTRATION.name)), isNull())
  }

  @Test
  fun `will consume a prison movement message out, update nhs`() {
    val message = "/messages/externalMovementOut.json".readResourceAsText()

    // wait until our queue has been purged
    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }

    awsSqsClient.sendMessage(queueUrl, message)

    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }
    await untilCallTo { prisonApi.prisonRequestCountFor("/api/prisoners/A5089EY/full-status") } matches { it == 1 }
    await untilCallTo { prisonRegisterApi.registerRequestCountFor("/gp/prison/MDI") } matches { it == 1 }
    await untilCallTo { nhsApi.nhsPostCountFor("/patient-upsert") } matches { it == 1 }

//    verify(telemetryClient).trackEvent(eq("p2nhs-prisoner-new"), any(), isNull())
//    verify(telemetryClient).trackEvent(eq("p2nhs-send-to-nhs"), eq(mapOf("nomsId" to "A5089EY", "change-type" to ChangeType.DEDUCTION.name)), isNull())
  }

  @Test
  fun `will consume a prisoner imprisonment status change and update nhs`() {
    val message = "/messages/imprisonmentStatusChanged.json".readResourceAsText()

    // wait until our queue has been purged
    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }

    awsSqsClient.sendMessage(queueUrl, message)

    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }
    await untilCallTo { prisonApi.prisonRequestCountFor("/api/bookings/1900835") } matches { it == 1 }
    await untilCallTo { prisonApi.prisonRequestCountFor("/api/prisoners/A6089DY/full-status") } matches { it == 1 }
    await untilCallTo { prisonRegisterApi.registerRequestCountFor("/gp/prison/MDI") } matches { it == 1 }
    await untilCallTo { nhsApi.nhsPostCountFor("/patient-upsert") } matches { it == 1 }

//    verify(telemetryClient).trackEvent(eq("p2nhs-prisoner-new"), any(), isNull())
//    verify(telemetryClient).trackEvent(eq("p2nhs-send-to-nhs"), eq(mapOf("nomsId" to "A6089DY", "change-type" to ChangeType.AMENDMENT.name)), isNull())
  }

  @Test
  fun `will consume a offender update change and update nhs`() {
    val message = "/messages/offenderDetailsChanged.json".readResourceAsText()

    // wait until our queue has been purged
    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }

    awsSqsClient.sendMessage(queueUrl, message)

    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }
    await untilCallTo { prisonApi.prisonRequestCountFor("/api/bookings/offenderNo/A7089EY") } matches { it == 1 }
    await untilCallTo { prisonApi.prisonRequestCountFor("/api/prisoners/A7089EY/full-status") } matches { it == 1 }
    await untilCallTo { prisonRegisterApi.registerRequestCountFor("/gp/prison/MDI") } matches { it == 1 }
    await untilCallTo { nhsApi.nhsPostCountFor("/patient-upsert") } matches { it == 1 }

//    verify(telemetryClient).trackEvent(eq("p2nhs-prisoner-new"), any(), isNull())
//    verify(telemetryClient).trackEvent(eq("p2nhs-send-to-nhs"), eq(mapOf("nomsId" to "A7089EY", "change-type" to ChangeType.AMENDMENT.name)), isNull())
  }

  @Test
  fun `will not update nhs when no change to data set`() {
    val message = "/messages/bookingReassigned.json".readResourceAsText()

    // wait until our queue has been purged
    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }

    awsSqsClient.sendMessage(queueUrl, message)
    awsSqsClient.sendMessage(queueUrl, message)

    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 0 }
    await untilCallTo { prisonApi.prisonRequestCountFor("/api/bookings/1900836") } matches { it == 2 }
    await untilCallTo { prisonApi.prisonRequestCountFor("/api/prisoners/A6089DX/full-status") } matches { it == 2 }
    await untilCallTo { prisonRegisterApi.registerRequestCountFor("/gp/prison/MDI") } matches { it == 1 }
    await untilCallTo { nhsApi.nhsPostCountFor("/patient-upsert") } matches { it == 1 }

//    verify(telemetryClient).trackEvent(eq("p2nhs-prisoner-new"), any(), isNull())
//    verify(telemetryClient).trackEvent(eq("p2nhs-send-to-nhs"), eq(mapOf("nomsId" to "A6089DX", "change-type" to ChangeType.AMENDMENT.name)), isNull())
  }
}

private fun String.readResourceAsText(): String {
  return MessageIntegrationTest::class.java.getResource(this).readText()
}
