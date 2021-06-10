package uk.gov.justice.digital.hmpps.prisontonhs.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.AnonymousAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.CreateQueueRequest
import com.amazonaws.services.sqs.model.QueueAttributeName
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class LocalStackConfig {

  companion object {
    val logger = LoggerFactory.getLogger(this::class.java)
  }

  @Bean("awsSqsClient")
  @Primary
  @ConditionalOnProperty(name = ["sqs.provider"], havingValue = "localstack")
  fun sqsClient(sqsConfigProperties: SqsConfigProperties, dlqSqsClient: AmazonSQS): AmazonSQSAsync =
    amazonSQSAsync(sqsConfigProperties.localstackUrl, sqsConfigProperties.region)
      .also { sqsClient -> createMainQueue(sqsClient, dlqSqsClient, sqsConfigProperties) }
      .also { logger.info("Created sqs client for queue ${sqsConfigProperties.queueName}") }

  @Bean("awsSqsDlqClient")
  @ConditionalOnProperty(name = ["sqs.provider"], havingValue = "localstack")
  fun sqsDlqClient(sqsConfigProperties: SqsConfigProperties): AmazonSQS =
    amazonSQS(sqsConfigProperties.localstackUrl, sqsConfigProperties.region)
      .also { dlqSqsClient -> dlqSqsClient.createQueue(sqsConfigProperties.dlqName) }
      .also { logger.info("Created dlq sqs client for dlq ${sqsConfigProperties.dlqName}") }

  private fun amazonSQSAsync(serviceEndpoint: String, region: String): AmazonSQSAsync =
    AmazonSQSAsyncClientBuilder.standard()
      .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(serviceEndpoint, region))
      .withCredentials(AWSStaticCredentialsProvider(AnonymousAWSCredentials()))
      .build()

  private fun amazonSQS(serviceEndpoint: String, region: String): AmazonSQS =
    AmazonSQSClientBuilder.standard()
      .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(serviceEndpoint, region))
      .withCredentials(AWSStaticCredentialsProvider(AnonymousAWSCredentials()))
      .build()

  private fun createMainQueue(
    queueSqsClient: AmazonSQSAsync,
    dlqSqsClient: AmazonSQS,
    sqsConfigProperties: SqsConfigProperties,
  ) =
    dlqSqsClient.getQueueUrl(sqsConfigProperties.dlqName).queueUrl
      .let { dlqQueueUrl ->
        dlqSqsClient.getQueueAttributes(
          dlqQueueUrl,
          listOf(QueueAttributeName.QueueArn.toString())
        ).attributes["QueueArn"]!!
      }
      .also { queueArn ->
        queueSqsClient.createQueue(
          CreateQueueRequest(sqsConfigProperties.queueName).withAttributes(
            mapOf(
              QueueAttributeName.RedrivePolicy.toString() to
                """{"deadLetterTargetArn":"$queueArn","maxReceiveCount":"5"}"""
            )
          )
        )
      }

  @Bean("queueMessagingTemplate")
  @ConditionalOnProperty(name = ["sqs.provider"], havingValue = "localstack")
  fun queueMessagingTemplate(amazonSQSAsync: AmazonSQSAsync?): QueueMessagingTemplate? =
    QueueMessagingTemplate(amazonSQSAsync)
}
