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
    private Integer maxRetryTimes

    @PostConstruct
    void init() {
        retryWaitTime = grailsApplication.config.getProperty('com.atlasgenetics.hermes.retryInterval', Long, 10000L)
        maxRetryTimes =  grailsApplication.config.getProperty('com.atlasgenetics.hermes.retryTimes', Integer, 5)
    }

    boolean sendMessage(MessageCommand message) {
        HttpResponseWrapper response = HttpUtils.attemptInitialSend(message)
        if (response.failed) {
            FailedMessage failedMessage = failedMessageManagerService.createFailedMessage(message, response.statusCode)
            sleep(retryWaitTime)
            return retryFailedMessage(failedMessage, message, response)
        } else {
            // Custom response handling
            responseHandler.handleResponse(response)
            return true
        }
    }

    /**
     * Please note that this method assumes
     * @param message
     * @param command
     * @param responseWrapper
     * @return true if the message was sent successfully, false otherwise
     */
    boolean retryFailedMessage(FailedMessage message, MessageCommand command = null,
                               HttpResponseWrapper responseWrapper = null) {
        if (!message.invalid) {
            // Create objects that will not be provided by retry jobs
            if (!command) command = new MessageCommand(message.messageData)
            if (!responseWrapper) responseWrapper = new HttpResponseWrapper(statusCode: message.statusCode)
            // Perform retry operation
            responseWrapper = HttpUtils.retryMessage(command, maxRetryTimes, retryWaitTime, responseWrapper)
            // Custom response handling
            responseHandler.handleResponse(responseWrapper)
            if (responseWrapper.succeeded) {
                failedMessageManagerService.purgeMessage(message)
                return true
            } else {
                failedMessageManagerService.completeFailedRetryProcess(message, responseWrapper.statusCode)
                return false
            }
        } else {
            // Custom response handling
            if (responseWrapper) responseHandler.handleResponse(responseWrapper)
            return false
        }
    }

}
