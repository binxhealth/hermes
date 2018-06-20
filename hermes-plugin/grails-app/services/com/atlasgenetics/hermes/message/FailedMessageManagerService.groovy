package com.atlasgenetics.hermes.message

import com.atlasgenetics.hermes.utils.RestUtils
import grails.gorm.transactions.Transactional
import groovy.transform.Synchronized
import org.springframework.http.HttpStatus

@Transactional
class FailedMessageManagerService {

    FailedMessage createFailedMessage(MessageCommand messageData, HttpStatus status) {
        FailedMessage message = new FailedMessage()
        message.data = messageData
        message.locked = true
        message.invalid = RestUtils.isInvalid(status)
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
