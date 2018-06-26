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
        message.statusCode = statusCode
        message.save(failOnError: true)
    }

    @Synchronized
    Set<FailedMessage> gatherAndLockFailedMessagesForRetry(Integer maxMessagesToRetry = null) {
        Set<FailedMessage> messages = FailedMessage.createCriteria().list {
            ge('statusCode', 500)
            if (maxMessagesToRetry) maxResults(maxMessagesToRetry)
        } as Set<FailedMessage>
        messages*.lock()
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
        message.save(failOnError: true)
    }

}
