package com.atlasgenetics.hermes.message

import grails.gorm.transactions.Transactional
import groovy.transform.Synchronized

@Transactional
class FailedMessageManagerService {

    FailedMessage createFailedMessage(MessageCommand messageData, int statusCode) {
        FailedMessage message = new FailedMessage()
        message.messageData = messageData.properties
        message.locked = true
        message.statusCode = statusCode
        message.save(failOnError: true)
    }

    @Synchronized
    Set<FailedMessage> gatherAndLockFailedMessagesForRetry() {
        Set<FailedMessage> messages = FailedMessage.findAllByLockedAndInvalid(false, false)
        messages*.locked = true
        return messages
    }

    void purgeMessage(FailedMessage message) {
        message.delete(failOnError: true)
    }

    void unlockMessage(FailedMessage message) {
        message.locked = false
        message.save(failOnError: true)
    }

}
