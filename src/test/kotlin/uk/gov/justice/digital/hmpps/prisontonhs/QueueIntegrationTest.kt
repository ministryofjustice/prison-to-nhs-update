package uk.gov.justice.digital.hmpps.prisontonhs

import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.prisontonhs.config.SqsConfigProperties
import uk.gov.justice.digital.hmpps.prisontonhs.integration.IntegrationTest

@ActiveProfiles(profiles = ["test", "test-queue"])
abstract class QueueIntegrationTest : IntegrationTest() {

  @Autowired
  protected lateinit var sqsConfigProperties: SqsConfigProperties

  fun getNumberOfMessagesCurrentlyOnQueue(): Int? {
    val queueAttributes = awsSqsClient.getQueueAttributes(sqsConfigProperties.queueName.queueUrl(), listOf("ApproximateNumberOfMessages"))
    return queueAttributes.attributes["ApproximateNumberOfMessages"]?.toInt()
  }

  fun prisonRequestCountFor(url: String) = prisonMockServer.findAll(getRequestedFor(urlEqualTo(url))).count()

  fun registerRequestCountFor(url: String) = prisonRegisterMockServer.findAll(getRequestedFor(urlEqualTo(url))).count()

  fun nhsPostCountFor(url: String) = nhsMockServer.findAll(postRequestedFor(urlEqualTo(url))).count()

  fun String.queueUrl(): String = awsSqsClient.getQueueUrl(this).queueUrl
}
