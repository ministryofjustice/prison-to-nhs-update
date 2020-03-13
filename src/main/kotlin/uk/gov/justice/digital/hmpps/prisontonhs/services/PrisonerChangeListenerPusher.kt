package uk.gov.justice.digital.hmpps.prisontonhs.services

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service


@Service
open class PrisonerChangeListenerPusher(
    private val prisonerPatientUpdateService: PrisonerPatientUpdateService
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
    val gson: Gson = GsonBuilder().create()
  }

  @JmsListener(destination = "\${sqs.queue.name}")
  open fun pushPrisonUpdateToProbation(requestJson: String?) {
    log.debug(requestJson)
    val (message, messageId, messageAttributes) = gson.fromJson(requestJson, Message::class.java)
    val eventType = messageAttributes.eventType.Value
    log.info("Received message $messageId type $eventType")

    when (eventType) {
      "EXTERNAL_MOVEMENT_RECORD-INSERTED" -> prisonerPatientUpdateService.externalMovement(fromJson(message))
      else -> prisonerPatientUpdateService.offenderChange(fromJson(message))
    }

  }

  private inline fun <reified T> fromJson(message: String): T {
    return gson.fromJson(message, T::class.java)
  }
}

data class EventType(val Value: String)
data class MessageAttributes(val eventType: EventType)
data class Message(val Message: String, val MessageId: String, val MessageAttributes: MessageAttributes)

data class ExternalPrisonerMovementMessage(val bookingId: Long,
                                           val movementSeq: Long,
                                           val offenderIdDisplay: String,
                                           val fromAgencyLocationId: String,
                                           val toAgencyLocationId: String,
                                           val directionCode: String,
                                           val movementType: String)

data class OffenderChangedMessage(val bookingId: Long)
