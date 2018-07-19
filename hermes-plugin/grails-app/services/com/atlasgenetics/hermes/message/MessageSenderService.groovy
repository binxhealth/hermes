package com.atlasgenetics.hermes.message

import com.atlasgenetics.hermes.utils.RestUtils
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional

/**
 * This service orchestrates the sending of HTTP requests throughout the message send and retry process.
 *
 * @author Maura Warner
 */
@Transactional
class MessageSenderService {

    FailedMessageManagerService failedMessageManagerService
    GrailsApplication grailsApplication

    boolean sendMessage(MessageCommand message) {
        int status = RestUtils.attemptInitialSend(message)
        if (RestUtils.isFailureCode(status)) {
            FailedMessage failedMessage = failedMessageManagerService.createFailedMessage(message, status)
            sleep(retryWaitTime)
            return retryFailedMessage(failedMessage, message)
        }
        return true
    }

    boolean retryFailedMessage(FailedMessage message, MessageCommand command = null) {
        if (!command) {
            command = new MessageCommand(message.messageData)
        }
        if (!message.invalid) {
            int statusCode = RestUtils.retryMessage(command, maxRetryTimes, retryWaitTime, message.statusCode)
            if (RestUtils.isSuccessCode(statusCode)) {
                failedMessageManagerService.purgeMessage(message)
                return true
            } else {
                failedMessageManagerService.completeFailedRetryProcess(message, statusCode)
                return false
            }
        } else {
            return false
        }
    }

    private Long getRetryWaitTime() {
        grailsApplication.config.getProperty('com.atlasgenetics.hermes.retryInterval', Long, 10000L)
    }

    private int getMaxRetryTimes() {
        grailsApplication.config.getProperty('com.atlasgenetics.hermes.retryTimes', Integer, 5)
    }

}
