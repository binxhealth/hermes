package com.atlasgenetics.hermes.message

import com.atlasgenetics.hermes.response.HttpResponseWrapper
import com.atlasgenetics.hermes.response.ResponseHandler
import com.atlasgenetics.hermes.utils.HttpUtils
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional

import javax.annotation.PostConstruct

/**
 * This service orchestrates the sending of HTTP requests throughout the message send and retry process.
 *
 * @author Maura Warner
 */
@Transactional
class MessageSenderService {

    FailedMessageManagerService failedMessageManagerService
    GrailsApplication grailsApplication
    ResponseHandler responseHandler

    private Long retryWaitTime
    private Integer maxRetryAttempts

    @PostConstruct
    void init() {
        retryWaitTime = grailsApplication.config.getProperty('com.atlasgenetics.hermes.retryInterval', Long, 10000L)
        maxRetryAttempts =  grailsApplication.config.getProperty('com.atlasgenetics.hermes.retryTimes', Integer, 5)
    }

    boolean sendNewMessage(MessageCommand message) {
        FailedMessage failedMessage = null
        HttpResponseWrapper response = HttpUtils.makeRequest(message)
        if (response.failed) {
            failedMessage = failedMessageManagerService.createFailedMessage(message, response.statusCode)
            if (!failedMessage.invalid) {
                sleep(retryWaitTime)
                return retryFailedMessage(failedMessage, message)
            }
        }
        // Custom response handling
        responseHandler.handleResponse(response, message, failedMessage)
        return response.succeeded
    }

    boolean retryFailedMessage(FailedMessage message, MessageCommand messageCommand = null) {
        if (!message.invalid) {
            if (!messageCommand) messageCommand = new MessageCommand(message.messageData)
            HttpResponseWrapper response = HttpUtils.retryMessage(messageCommand, message.statusCode,
                    maxRetryAttempts, retryWaitTime)
            if (response.succeeded) {
                failedMessageManagerService.purgeMessage(message)
                // Custom response handling
                responseHandler.handleResponse(response, messageCommand, null)
            } else {
                failedMessageManagerService.completeFailedRetryProcess(message, response.statusCode)
                // Custom response handling
                responseHandler.handleResponse(response, messageCommand, message)
            }
            return response.succeeded
        } else {
            throw new IllegalArgumentException("FailedMessage with HTTP status code ${message.statusCode} is invalid" +
                    " and not eligible for retry")
        }
    }

}
