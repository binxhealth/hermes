package com.atlasgenetics.hermes.message

import grails.gorm.transactions.Transactional
import groovy.transform.Synchronized

/**
 * This service handles any and all changes to FailedMessage data, including creation and deletion of specific messages.
 *
 * @author Maura Warner
 */
@Transactional
class FailedMessageManagerService {

    FailedMessage createFailedMessage(MessageCommand messageData, int statusCode) {
        FailedMessage message = new FailedMessage()
        message.messageData = messageData.toMap()
        message.locked = true
        message.statusCode = statusCode
        message.save(failOnError: true)
    }

    @Synchronized
    Set<FailedMessage> gatherAndLockFailedMessagesForRetry() {
        Set<FailedMessage> messages = FailedMessage.findAllByLockedAndStatusCodeGreaterThan(false, 499)
        messages*.locked = true
        messages*.save(failOnError: true)
        return messages
    }

    void purgeMessage(FailedMessage message) {
        message.delete(failOnError: true)
    }

    void purgeMessages(Set<FailedMessage> messages) {
        messages*.delete(failOnError: true)
    }

    void completeFailedRetryProcess(FailedMessage message, int finalStatusCode) {
        message.statusCode = finalStatusCode
        message.locked = false
        message.save(failOnError: true)
    }

}
