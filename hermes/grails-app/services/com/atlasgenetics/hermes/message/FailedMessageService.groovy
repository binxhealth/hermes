package com.atlasgenetics.hermes.message

import grails.gorm.transactions.Transactional

@Transactional
class FailedMessageService {

    FailedMessage createFailedMessage(Map messageData) {
        FailedMessage message = new FailedMessage()
        message.data = messageData
        message.locked = true
        message.save(failOnError: true)
    }

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
