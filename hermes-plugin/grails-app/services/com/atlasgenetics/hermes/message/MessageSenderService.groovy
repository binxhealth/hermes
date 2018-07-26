package com.atlasgenetics.hermes.message

import com.atlasgenetics.hermes.response.HttpResponse

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

    private Long retryWaitTime
    private Integer maxRetryAttempts

    @PostConstruct
    void init() {
        retryWaitTime = grailsApplication.config.getProperty('com.atlasgenetics.hermes.retryInterval', Long, 10000L)
        maxRetryAttempts =  grailsApplication.config.getProperty('com.atlasgenetics.hermes.retryTimes', Integer, 5)
    }

    HttpResponse sendNewMessage(MessageCommand message) {
        HttpResponse response = HttpUtils.makeRequest(message)
        if (response.failed) {
            FailedMessage failedMessage = failedMessageManagerService.createFailedMessage(message, response.statusCode)
            if (!failedMessage.invalid) {
                sleep(retryWaitTime)
                return retryFailedMessage(failedMessage, message)
            }
        }
        return response
    }

    HttpResponse retryFailedMessage(FailedMessage message, MessageCommand messageCommand = null) {
        if (!message.invalid) {
            if (!messageCommand) messageCommand = new MessageCommand(message.messageData)
            HttpResponse response = HttpUtils.retryMessage(messageCommand, message.statusCode,
                    maxRetryAttempts, retryWaitTime)
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
