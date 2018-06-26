package com.atlasgenetics.hermes.message

import grails.gorm.transactions.Transactional

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

    /**
     * Locates all FailedMessages currently eligible for retry.  Messages that failed with 3xx or 4xx error codes
     * are ineligible for retry as they are invalid; only messages that failed with 5xx error codes should be
     * retried.
     * @return FailedMessages to retry
     */
    @Transactional(readOnly = true)
    Set<FailedMessage> gatherFailedMessagesForRetry() {
        Set<FailedMessage> messages = FailedMessage.findAllByStatusCodeGreaterThanEquals(500)
        return messages
    }

}
