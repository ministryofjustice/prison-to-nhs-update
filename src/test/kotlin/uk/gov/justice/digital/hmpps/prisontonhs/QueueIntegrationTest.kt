package uk.gov.justice.digital.hmpps.prisontonhs

import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.prisontonhs.health.IntegrationTest


@ActiveProfiles(profiles = ["test", "test-queue"])
class QueueIntegrationTest : IntegrationTest() {

    @Autowired
    lateinit var queueUrl: String

    fun getNumberOfMessagesCurrentlyOnQueue(): Int? {
        val queueAttributes = awsSqsClient.getQueueAttributes(queueUrl, listOf("ApproximateNumberOfMessages"))
        return queueAttributes.attributes["ApproximateNumberOfMessages"]?.toInt()
    }

    fun prisonRequestCountFor(url: String) = prisonMockServer.findAll(getRequestedFor(urlEqualTo(url))).count()

    fun estateRequestCountFor(url: String)= prisonEstateMockServer.findAll(getRequestedFor(urlEqualTo(url))).count()

    fun nhsPostCountFor(url: String)= nhsMockServer.findAll(postRequestedFor(urlEqualTo(url))).count()

}
