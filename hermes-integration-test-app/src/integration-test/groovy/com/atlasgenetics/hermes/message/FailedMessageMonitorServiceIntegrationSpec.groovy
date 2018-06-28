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
    FailedMessage invalidGet
    FailedMessage invalidHead
    FailedMessage redirectGet
    FailedMessage invalidDeleteOld
    // www.example.com
    FailedMessage validPost
    FailedMessage redirectPut
    FailedMessage validPutOld


    def setup() {
        FailedMessage.withTransaction {
            use(TimeCategory) {
                invalidGet = new FailedMessage(
                        statusCode: 400,
                        messageData: [
                                url: "$TEST_DOT_COM/foo".toString(),
                                httpMethod: 'GET'
                        ]).save()
                invalidHead = new FailedMessage(
                        statusCode: 404,
                        messageData: [
                                url: "$TEST_DOT_COM/bar".toString(),
                                httpMethod: 'HEAD'
                        ]).save()
                validPost = new FailedMessage(
                        statusCode: 500,
                        messageData: [
                                url: "$EXAMPLE_DOT_COM/foo".toString(),
                                httpMethod: 'POST'
                        ]).save()
                redirectPut = new FailedMessage(
                        statusCode: 302,
                        messageData: [
                                url: "$EXAMPLE_DOT_COM/bar".toString(),
                                httpMethod: 'PUT'
                        ]).save()
                redirectGet = new FailedMessage(
                        statusCode: 302,
                        messageData: [
                                url: "$TEST_DOT_COM/foo".toString(),
                                httpMethod: 'GET'
                        ]).save()

                // Cannot use map constructor to spoof dateCreated
                invalidDeleteOld = new FailedMessage(
                        statusCode: 404,
                        messageData: [
                                url: "$TEST_DOT_COM/baz".toString(),
                                httpMethod: 'DELETE'
                        ]).save()
                invalidDeleteOld.dateCreated = new Date() - 3.days
                invalidDeleteOld.save()

                validPutOld = new FailedMessage(
                        dateCreated: new Date() - 2.days,
                        statusCode: 500,
                        messageData: [
                                url: "$EXAMPLE_DOT_COM/baz".toString(),
                                httpMethod: 'PUT'
                        ]).save()
                validPutOld.dateCreated = new Date() - 2.days
                validPutOld.save()
            }
        }
    }

    def cleanup() {
        FailedMessage.withTransaction {
            invalidDeleteOld.delete()
            invalidHead.delete()
            invalidGet.delete()
            validPutOld.delete()
            validPost.delete()
            redirectPut.delete()
            redirectGet.delete()
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
        results.find { it.id == invalidGet.id }
        results.find { it.id == invalidHead.id }
        results.find { it.id == invalidDeleteOld.id }
        !results.find { it.id == validPost.id }
        !results.find { it.id == validPutOld.id }
        !results.find { it.id == redirectGet.id }
        !results.find { it.id == redirectPut.id }
    }

    void "test getRedirectedMessages"() {
        when: "we query for messages that failed with a 3xx status code"
        List<FailedMessage> results = failedMessageMonitorService.getRedirectedMessages()

        then: "only messages with the desired status codes are returned"
        results
        !results.find { it.id == invalidGet.id }
        !results.find { it.id == invalidHead.id }
        !results.find { it.id == invalidDeleteOld.id }
        !results.find { it.id == validPost.id }
        !results.find { it.id == validPutOld.id }
        results.find { it.id == redirectGet.id }
        results.find { it.id == redirectPut.id }
    }

    void "test getInvalidMessages"() {
        when: "we query for messages that failed with a 3xx or 4xx status code"
        List<FailedMessage> results = failedMessageMonitorService.getInvalidMessages()

        then: "only messages with the desired status codes are returned"
        results
        results.find { it.id == invalidGet.id }
        results.find { it.id == invalidHead.id }
        results.find { it.id == invalidDeleteOld.id }
        !results.find { it.id == validPost.id }
        !results.find { it.id == validPutOld.id }
        results.find { it.id == redirectGet.id }
        results.find { it.id == redirectPut.id }
    }

    void "test getValidMessages"() {
        when: "we query for messages that failed with a 5xx status code"
        List<FailedMessage> results = failedMessageMonitorService.getValidMessages()

        then: "only messages with the desired status codes are returned"
        results
        !results.find { it.id == invalidGet.id }
        !results.find { it.id == invalidHead.id }
        !results.find { it.id == invalidDeleteOld.id }
        results.find { it.id == validPost.id }
        results.find { it.id == validPutOld.id }
        !results.find { it.id == redirectGet.id }
        !results.find { it.id == redirectPut.id }
    }

    void "test getMessagesMoreThanOneDayOld"() {
        when: "we query for messages that were added to the DB over 24 hours ago"
        List<FailedMessage> results = failedMessageMonitorService.getMessagesMoreThanOneDayOld()

        then: "only old messages are returned"
        results
        !results.find { it.id == invalidGet.id }
        !results.find { it.id == invalidHead.id }
        results.find { it.id == invalidDeleteOld.id }
        !results.find { it.id == validPost.id }
        results.find { it.id == validPutOld.id }
        !results.find { it.id == redirectGet.id }
        !results.find { it.id == redirectPut.id }
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
        results.find { it.id == invalidGet.id }
        results.find { it.id == invalidHead.id }
        !results.find { it.id == invalidDeleteOld.id }
        !results.find { it.id == validPost.id }
        !results.find { it.id == validPutOld.id }
        !results.find { it.id == redirectGet.id }
        !results.find { it.id == redirectPut.id }
    }

    void "test httpMethod, orderBy"() {
        given: "the args for the criteria query"
        Map args = [
                httpMethod: 'PUT',
                orderByProp: 'statusCode'
        ]

        when: "we query for messages"
        List<FailedMessage> results = failedMessageMonitorService.listFailedMessages(args)

        then: "only the expected messages are returned"
        results
        !results.find { it.id == invalidGet.id }
        !results.find { it.id == invalidHead.id }
        !results.find { it.id == invalidDeleteOld.id }
        !results.find { it.id == validPost.id }
        results.find { it.id == validPutOld.id }
        !results.find { it.id == redirectGet.id }
        results.find { it.id == redirectPut.id }

        and: "the result set is sorted correctly"
        results == results.sort { it.statusCode }
    }
}
