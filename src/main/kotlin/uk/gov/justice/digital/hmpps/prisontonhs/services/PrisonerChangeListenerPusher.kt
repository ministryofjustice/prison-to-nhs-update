package uk.gov.justice.digital.hmpps.prisontonhs.services

import com.google.gson.Gson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service

@Service
class PrisonerChangeListenerPusher(
  private val prisonerPatientUpdateService: PrisonerPatientUpdateService,
  @Qualifier("gson") private val gson: Gson
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @JmsListener(destination = "#{@'sqs-uk.gov.justice.digital.hmpps.prisontonhs.config.SqsConfigProperties'.queueName}")
  fun pushPrisonUpdateToNhs(requestJson: String?) {
    log.debug(requestJson)
    val (message, messageId, messageAttributes) = gson.fromJson(requestJson, Message::class.java)
    val eventType = messageAttributes.eventType.Value
    log.info("Received message {} type {}", messageId, eventType)

    when (eventType) {
      "EXTERNAL_MOVEMENT_RECORD-INSERTED" -> prisonerPatientUpdateService.externalMovement(fromJson(message))
      "BED_ASSIGNMENT_HISTORY-INSERTED" -> prisonerPatientUpdateService.offenderBookingChange(fromJson(message))
      "IMPRISONMENT_STATUS-CHANGED" -> prisonerPatientUpdateService.offenderBookingChange(fromJson(message))
      "SENTENCE_DATES-CHANGED" -> prisonerPatientUpdateService.offenderBookingChange(fromJson(message))
      "ASSESSMENT-CHANGED" -> prisonerPatientUpdateService.offenderBookingChange(fromJson(message))
      "OFFENDER_BOOKING-REASSIGNED" -> prisonerPatientUpdateService.offenderBookingChange(fromJson(message))
      "OFFENDER_BOOKING-CHANGED" -> prisonerPatientUpdateService.offenderBookingChange(fromJson(message))
      "BOOKING_NUMBER-CHANGED" -> prisonerPatientUpdateService.offenderBookingChange(fromJson(message))
      "OFFENDER_DETAILS-CHANGED" -> prisonerPatientUpdateService.offenderChange(fromJson(message))
      "OFFENDER-UPDATED" -> prisonerPatientUpdateService.offenderChange(fromJson(message))

      else -> log.warn("We received a message of event type {} which I really wasn't expecting", eventType)
    }
  }

  private inline fun <reified T> fromJson(message: String): T {
    return gson.fromJson(message, T::class.java)
  }
}

data class EventType(val Value: String)
data class MessageAttributes(val eventType: EventType)
data class Message(val Message: String, val MessageId: String, val MessageAttributes: MessageAttributes)

data class ExternalPrisonerMovementMessage(
  val bookingId: Long,
  val movementSeq: Long,
  val offenderIdDisplay: String,
  val fromAgencyLocationId: String,
  val toAgencyLocationId: String,
  val directionCode: String,
  val movementType: String
)

data class OffenderBookingChangedMessage(val bookingId: Long)

data class OffenderChangedMessage(val offenderIdDisplay: String)
