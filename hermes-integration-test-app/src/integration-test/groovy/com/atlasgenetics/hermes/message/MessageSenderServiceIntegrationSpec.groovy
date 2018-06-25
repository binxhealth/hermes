package com.atlasgenetics.hermes.message

import com.stehno.ersatz.ErsatzServer
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import hermes.integration.test.app.utils.TestUtils
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import spock.lang.Specification

@Integration
@Rollback
class MessageSenderServiceIntegrationSpec extends Specification {

    def messageSenderService
    def grailsApplication

    static final String TEST_URI = "/endpoint"

    void "test send message - succeeds on first try"() {
        given: "a mock server expecting the message we want to send"
        ErsatzServer mock = TestUtils.newErsatzServer()
        mock.expectations {
            get(TEST_URI) {
                responder {
                    code HttpStatus.OK.value()
                }

                called 1
            }
        }

        and: "the appropriate message data"
        MessageCommand cmd = new MessageCommand()
        cmd.url = "${mock.getHttpUrl()}$TEST_URI"
        cmd.httpMethod = HttpMethod.GET

        when: "we try to send the message"
        boolean sent = messageSenderService.sendMessage(cmd)

        then: "it succeeds"
        sent

        and: "the mock server was called as expected"
        mock.verify()
    }

    void "test send message - fails on first try, succeeds on retry"() {
        given: "a mock server expecting the message we want to send"
        ErsatzServer mock = TestUtils.newErsatzServer()
        mock.expectations {
            get(TEST_URI) {
                responder {
                    code HttpStatus.INTERNAL_SERVER_ERROR.value()
                }
                responder {
                    code HttpStatus.OK.value()
                }

                called 2
            }
        }

        and: "the appropriate message data"
        MessageCommand cmd = new MessageCommand()
        cmd.url = "${mock.getHttpUrl()}$TEST_URI"
        cmd.httpMethod = HttpMethod.GET

        when: "we try to send the message"
        boolean sent = messageSenderService.sendMessage(cmd)

        then: "it succeeds"
        sent

        and: "the mock server was called as expected"
        mock.verify()
    }

    void "test send message - fails"() {
        given: "a mock server expecting the message we want to send"
        String uri = TEST_URI + UUID.randomUUID().toString()
        ErsatzServer mock = TestUtils.newErsatzServer()
        mock.expectations {
            get(uri) {
                responder {
                    code HttpStatus.INTERNAL_SERVER_ERROR.value()
                }

                called 6
            }
        }

        and: "the appropriate message data"
        MessageCommand cmd = new MessageCommand()
        cmd.url = "${mock.getHttpUrl()}$uri"
        cmd.httpMethod = HttpMethod.GET

        and: "override the retryInterval default so the test runs faster"
        grailsApplication.config.com.atlasgenetics.hermes.retryInterval = 0L

        when: "we try to send the message"
        boolean sent  = FailedMessage.withSession { session ->
            boolean out = messageSenderService.sendMessage(cmd)
            session.flush()
            return out
        }

        then: "it fails"
        !sent

        and: "the mock server was called as expected"
        mock.verify()

        and: "a FailedMessage was created in the DB"
        def results = FailedMessage.withSession {
            FailedMessage.withCriteria {
                pgJsonHasFieldValue 'messageData', 'url', cmd.url
                pgJsonHasFieldValue 'messageData', 'httpMethod', cmd.httpMethod
                eq 'statusCode', HttpStatus.INTERNAL_SERVER_ERROR.value()
                eq 'locked', false
            }
        }
        results
    }

    void "test failed message - verify retryTimes config property"() {
        given: "a mock server expecting the message we want to send"
        ErsatzServer mock = TestUtils.newErsatzServer()
        mock.expectations {
            get(TEST_URI) {
                responder {
                    code HttpStatus.INTERNAL_SERVER_ERROR.value()
                }

                called 11
            }
        }

        and: "the appropriate message data"
        MessageCommand cmd = new MessageCommand()
        cmd.url = "${mock.getHttpUrl()}$TEST_URI"
        cmd.httpMethod = HttpMethod.GET

        and: "override the retryInterval default so the test runs faster"
        grailsApplication.config.com.atlasgenetics.hermes.retryInterval = 0L

        and: "the retryTimes configuration property set to override the default value"
        grailsApplication.config.com.atlasgenetics.hermes.retryTimes = 10

        when: "we try to send the failing message"
        messageSenderService.sendMessage(cmd)

        then: "the mock server will be called the expected number of times, rather than the default"
        mock.verify()
    }

}
