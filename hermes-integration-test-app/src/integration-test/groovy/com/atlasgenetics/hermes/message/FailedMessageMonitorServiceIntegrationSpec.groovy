package com.atlasgenetics.hermes.message

import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import groovy.time.TimeCategory
import spock.lang.Specification

@Integration
@Rollback
class FailedMessageMonitorServiceIntegrationSpec extends Specification {

    FailedMessageMonitorService failedMessageMonitorService
    
    static final String TEST_DOT_COM = 'http://test.com'
    static final String EXAMPLE_DOT_COM = 'http://www.example.com'

    // test.com
    FailedMessage invalidUnlockedGet
    FailedMessage invalidLockedHead
    FailedMessage redirectLockedGet
    FailedMessage invalidUnlockedDeleteOld
    // www.example.com
    FailedMessage validUnlockedPost
    FailedMessage redirectUnlockedPut
    FailedMessage validUnlockedPutOld


    def setup() {
        FailedMessage.withTransaction {
            use(TimeCategory) {
                invalidUnlockedGet = new FailedMessage(
                        statusCode: 400,
                        locked: false,
                        messageData: [
                                url: "$TEST_DOT_COM/foo".toString(),
                                httpMethod: 'GET'
                        ]).save()
                invalidLockedHead = new FailedMessage(
                        statusCode: 404,
                        locked: true,
                        messageData: [
                                url: "$TEST_DOT_COM/bar".toString(),
                                httpMethod: 'HEAD'
                        ]).save()
                validUnlockedPost = new FailedMessage(
                        statusCode: 500,
                        locked: false,
                        messageData: [
                                url: "$EXAMPLE_DOT_COM/foo".toString(),
                                httpMethod: 'POST'
                        ]).save()
                redirectUnlockedPut = new FailedMessage(
                        statusCode: 302,
                        locked: false,
                        messageData: [
                                url: "$EXAMPLE_DOT_COM/bar".toString(),
                                httpMethod: 'PUT'
                        ]).save()
                redirectLockedGet = new FailedMessage(
                        statusCode: 302,
                        locked: true,
                        messageData: [
                                url: "$TEST_DOT_COM/foo".toString(),
                                httpMethod: 'GET'
                        ]).save()

                // Cannot use map constructor to spoof dateCreated and lastUpdated
                invalidUnlockedDeleteOld = new FailedMessage(
                        statusCode: 404,
                        locked: false,
                        messageData: [
                                url: "$TEST_DOT_COM/baz".toString(),
                                httpMethod: 'DELETE'
                        ]).save()
                invalidUnlockedDeleteOld.dateCreated = new Date() - 3.days
                invalidUnlockedDeleteOld.save()

                validUnlockedPutOld = new FailedMessage(
                        dateCreated: new Date() - 2.days,
                        statusCode: 500,
                        locked: false,
                        messageData: [
                                url: "$EXAMPLE_DOT_COM/baz".toString(),
                                httpMethod: 'PUT'
                        ]).save()
                validUnlockedPutOld.dateCreated = new Date() - 2.days
                validUnlockedPutOld.save()
            }
        }
    }

    def cleanup() {
        FailedMessage.withTransaction {
            invalidUnlockedDeleteOld.delete()
            invalidLockedHead.delete()
            invalidUnlockedGet.delete()
            validUnlockedPutOld.delete()
            validUnlockedPost.delete()
            redirectUnlockedPut.delete()
            redirectLockedGet.delete()
        }
    }
    
    void "test getMessagesWithStatusCodeInRange"() {
        given: "a range of statusCodes"
        int greaterThan = 400
        int lessThan = 499
        
        when: "we query for messages with status codes in a certain range"
        List<FailedMessage> results = failedMessageMonitorService.getMessagesWithStatusCodeInRange(greaterThan, lessThan, [:])
        
        then: "only messages with the desired status codes are returned"
        results
        results.find { it.id == invalidUnlockedGet.id }
        results.find { it.id == invalidLockedHead.id }
        results.find { it.id == invalidUnlockedDeleteOld.id }
        !results.find { it.id == validUnlockedPost.id }
        !results.find { it.id == validUnlockedPutOld.id }
        !results.find { it.id == redirectLockedGet.id }
        !results.find { it.id == redirectUnlockedPut.id }
    }

    void "test getRedirectedMessages"() {
        when: "we query for messages that failed with a 3xx status code"
        List<FailedMessage> results = failedMessageMonitorService.getRedirectedMessages()

        then: "only messages with the desired status codes are returned"
        results
        !results.find { it.id == invalidUnlockedGet.id }
        !results.find { it.id == invalidLockedHead.id }
        !results.find { it.id == invalidUnlockedDeleteOld.id }
        !results.find { it.id == validUnlockedPost.id }
        !results.find { it.id == validUnlockedPutOld.id }
        results.find { it.id == redirectLockedGet.id }
        results.find { it.id == redirectUnlockedPut.id }
    }

    void "test getInvalidMessages"() {
        when: "we query for messages that failed with a 3xx or 4xx status code"
        List<FailedMessage> results = failedMessageMonitorService.getInvalidMessages()

        then: "only messages with the desired status codes are returned"
        results
        results.find { it.id == invalidUnlockedGet.id }
        results.find { it.id == invalidLockedHead.id }
        results.find { it.id == invalidUnlockedDeleteOld.id }
        !results.find { it.id == validUnlockedPost.id }
        !results.find { it.id == validUnlockedPutOld.id }
        results.find { it.id == redirectLockedGet.id }
        results.find { it.id == redirectUnlockedPut.id }
    }

    void "test getValidMessages"() {
        when: "we query for messages that failed with a 5xx status code"
        List<FailedMessage> results = failedMessageMonitorService.getValidMessages()

        then: "only messages with the desired status codes are returned"
        results
        !results.find { it.id == invalidUnlockedGet.id }
        !results.find { it.id == invalidLockedHead.id }
        !results.find { it.id == invalidUnlockedDeleteOld.id }
        results.find { it.id == validUnlockedPost.id }
        results.find { it.id == validUnlockedPutOld.id }
        !results.find { it.id == redirectLockedGet.id }
        !results.find { it.id == redirectUnlockedPut.id }
    }

    void "test getMessagesMoreThanOneDayOld"() {
        when: "we query for messages that were added to the DB over 24 hours ago"
        List<FailedMessage> results = failedMessageMonitorService.getMessagesMoreThanOneDayOld()

        then: "only old messages are returned"
        results
        !results.find { it.id == invalidUnlockedGet.id }
        !results.find { it.id == invalidLockedHead.id }
        results.find { it.id == invalidUnlockedDeleteOld.id }
        !results.find { it.id == validUnlockedPost.id }
        results.find { it.id == validUnlockedPutOld.id }
        !results.find { it.id == redirectLockedGet.id }
        !results.find { it.id == redirectUnlockedPut.id }
    }

    void "test createdAfter, urlRegex, statusCodes"() {
        given: "the args for the criteria query"
        Map args = [
                createdAfter: use(TimeCategory){ new Date() - 1.day },
                urlRegEx: "$TEST_DOT_COM%",
                statusCodes: [400, 404, 500]
        ]

        when: "we query for messages"
        List<FailedMessage> results = failedMessageMonitorService.listFailedMessages(args)

        then: "only the expected messages are returned"
        results
        results.find { it.id == invalidUnlockedGet.id }
        results.find { it.id == invalidLockedHead.id }
        !results.find { it.id == invalidUnlockedDeleteOld.id }
        !results.find { it.id == validUnlockedPost.id }
        !results.find { it.id == validUnlockedPutOld.id }
        !results.find { it.id == redirectLockedGet.id }
        !results.find { it.id == redirectUnlockedPut.id }
    }

    void "test httpMethod, orderBy, locked"() {
        given: "the args for the criteria query"
        Map args = [
                httpMethod: 'PUT',
                locked: false,
                orderByProp: 'statusCode'
        ]

        when: "we query for messages"
        List<FailedMessage> results = failedMessageMonitorService.listFailedMessages(args)

        then: "only the expected messages are returned"
        results
        !results.find { it.id == invalidUnlockedGet.id }
        !results.find { it.id == invalidLockedHead.id }
        !results.find { it.id == invalidUnlockedDeleteOld.id }
        !results.find { it.id == validUnlockedPost.id }
        results.find { it.id == validUnlockedPutOld.id }
        !results.find { it.id == redirectLockedGet.id }
        results.find { it.id == redirectUnlockedPut.id }

        and: "the result set is sorted correctly"
        results == results.sort { it.statusCode }
    }
}
