package com.atlasgenetics.hermes.message

import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import spock.lang.Specification

@Integration
@Rollback
class FailedMessageManagerServiceIntegrationSpec extends Specification {

    def failedMessageManagerService

    void "test gather failed messages for retry"() {
        given: "several FailedMessages"
        FailedMessage validUnlocked = new FailedMessage()
        validUnlocked.messageData = [foo: 'bar']
        validUnlocked.statusCode = 500
        validUnlocked.save()

        FailedMessage invalidUnlocked = new FailedMessage()
        invalidUnlocked.messageData = [foo: 'bar']
        invalidUnlocked.statusCode = 400
        invalidUnlocked.save()

        FailedMessage invalidLocked = new FailedMessage()
        invalidLocked.messageData = [foo: 'bar']
        invalidLocked.statusCode = 400
        invalidLocked.save()

        FailedMessage validLocked = new FailedMessage()
        validLocked.messageData = [foo: 'bar']
        validLocked.statusCode = 500
        validLocked.save(flush: true)

        when: "we gather failed messages for retry"
        Set<FailedMessage> messages = failedMessageManagerService.gatherAndLockFailedMessagesForRetry()

        then: "only eligible messages are returned"
        messages
        messages.contains(validUnlocked)
        !messages.contains(invalidUnlocked)
        !messages.contains(validLocked)
        !messages.contains(invalidLocked)
    }

    void "test create failed message"() {
        given: "A MessageComand"
        MessageCommand command = new MessageCommand()
        command.httpMethod = HttpMethod.GET
        command.url = "http://www.test.example.com"

        and: "an HTTP status code"
        int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value()

        when: "we use this data to create a new FailedMessage"
        FailedMessage failedMessage = failedMessageManagerService.createFailedMessage(command, statusCode)

        then: "all properties are saved as expected"
        failedMessage.save(flush: true) // flushing here to force GORM to cooperate in the test env
        failedMessage.id
        failedMessage.statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()
        failedMessage.dateCreated
        failedMessage.lastUpdated

        and: "the message data is still available"
        failedMessage.messageData
        failedMessage.messageData.url == command.url
        failedMessage.messageData.httpMethod == command.httpMethod
    }

}
