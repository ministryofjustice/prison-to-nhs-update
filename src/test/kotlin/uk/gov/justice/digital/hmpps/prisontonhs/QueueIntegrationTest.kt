package uk.gov.justice.digital.hmpps.prisontonhs

import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.prisontonhs.integration.IntegrationTest

@ActiveProfiles(profiles = ["test", "test-queue"])
abstract class QueueIntegrationTest : IntegrationTest() {

  fun getNumberOfMessagesCurrentlyOnQueue(): Int? {
    val queueAttributes = awsSqsClient.getQueueAttributes(queueUrl, listOf("ApproximateNumberOfMessages"))
    return queueAttributes.attributes["ApproximateNumberOfMessages"]?.toInt()
  }
}
