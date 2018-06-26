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
        FailedMessage valid = new FailedMessage()
        valid.messageData = [foo: 'bar']
        valid.statusCode = 500
        valid.save()

        FailedMessage invalid = new FailedMessage()
        invalid.messageData = [foo: 'bar']
        invalid.statusCode = 400
        invalid.save(flush: true)

        when: "we gather failed messages for retry"
        Set<FailedMessage> messages = failedMessageManagerService.gatherFailedMessagesForRetry()

        then: "only eligible messages are returned"
        messages
        messages.contains(valid)
        !messages.contains(invalid)
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
