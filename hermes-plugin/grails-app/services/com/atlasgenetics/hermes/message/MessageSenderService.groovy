package com.atlasgenetics.hermes.message

import com.atlasgenetics.hermes.response.HttpResponse
import grails.gorm.transactions.Transactional

/**
 * This service orchestrates the sending of HTTP requests throughout the message send and retry process.
 *
 * @author Maura Warner
 */
@Transactional
class MessageSenderService {

    def failedMessageManagerService
    def httpService

    HttpResponse sendNewMessage(MessageCommand message) {
        HttpResponse response = httpService.makeRequest(message)
        if (response.failed) {
            FailedMessage failedMessage = failedMessageManagerService.createFailedMessage(message, response.statusCode)
            if (!failedMessage.invalid) {
                return retryFailedMessage(failedMessage, message)
            }
        }
        return response
    }

    HttpResponse retryFailedMessage(FailedMessage message, MessageCommand messageCommand = null) {
        if (!message.invalid) {
            if (!messageCommand) messageCommand = new MessageCommand(message.messageData)
            HttpResponse response = httpService.retryMessage(messageCommand, message.statusCode)
            if (response.succeeded) {
                failedMessageManagerService.purgeMessage(message)
            } else {
                failedMessageManagerService.completeFailedRetryProcess(message, response.statusCode)
            }
            return response
        } else {
            throw new IllegalArgumentException("FailedMessage with HTTP status code ${message.statusCode} is invalid" +
                    " and not eligible for retry")
        }
    }

}
