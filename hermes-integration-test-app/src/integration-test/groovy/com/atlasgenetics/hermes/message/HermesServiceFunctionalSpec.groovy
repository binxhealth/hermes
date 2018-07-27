package com.atlasgenetics.hermes.message

import com.atlasgenetics.hermes.response.HermesResponseWrapper
import com.atlasgenetics.hermes.utils.HttpStatusUtils
import com.stehno.ersatz.ContentType as ErsatzContentType
import com.stehno.ersatz.ErsatzServer
import grails.testing.mixin.integration.Integration
import groovyx.net.http.ContentType
import groovyx.net.http.Method
import hermes.integration.test.app.utils.TestUtils
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification

@Integration
@Rollback
@Transactional
class HermesServiceFunctionalSpec extends Specification {

    def hermesService
    def httpService

    static final String QUERY_PARAM_KEY = "q"
    static final String QUERY_PARAM_VAL = "query"
    static final String TEST_URI = "/endpoint"
    static final Map TEST_BODY = [foo: "bar"]
    static final String TEST_HEADER_KEY = "Header"
    static final String TEST_HEADER_VAL = "Data"
    static final String RESPONSE_BODY_JSON = '{"foo":"bar"}'

    def setup() {
        httpService.init()
    }

    void "test successful message - verify GET e2e"() {
        given: "a mock server expecting the request we want to make"
        ErsatzServer mock = TestUtils.newErsatzServer()
        mock.expectations {
            get(TEST_URI) {
                header(TEST_HEADER_KEY, TEST_HEADER_VAL)
                query(QUERY_PARAM_KEY, QUERY_PARAM_VAL)
                responder {
                    code HttpStatus.OK.value()
                    content RESPONSE_BODY_JSON, ErsatzContentType.APPLICATION_JSON.value
                }

                called 1
            }
        }

        and: "the appropriate message data"
        String baseUrl = mock.getHttpUrl()
        Map headers = [:]
        headers[TEST_HEADER_KEY] = TEST_HEADER_VAL
        Map queryParams = [:]
        queryParams[QUERY_PARAM_KEY] = QUERY_PARAM_VAL
        String url = "$baseUrl$TEST_URI"

        when: "we send the message"
        HermesResponseWrapper response = FailedMessage.withSession { session ->
            HermesResponseWrapper out = hermesService.makeRequest(Method.GET, url, ContentType.JSON,
                    null, headers, queryParams)
            session.flush()
            return out
        }

        then: "it succeeds"
        response.succeeded

        and: "the response data is populated as expected"
        response.headers
        response.payload
        response.payload.foo == 'bar'
        !response.failedMessageId

        and: "the mock server was called as expected"
        mock.verify()

        and: "there is no FailedMessage object in the database for the message"
        def results = FailedMessage.withSession {
            FailedMessage.withCriteria {
                pgJsonHasFieldValue 'messageData', 'url', url
            }
        }
        !results
    }

    void "test successful message - verify PUT e2e"() {
        given: "a mock server expecting the request we want to make"
        ErsatzServer mock = TestUtils.newErsatzServer()
        mock.expectations {
            put(TEST_URI) {
                header(TEST_HEADER_KEY, TEST_HEADER_VAL)
                query(QUERY_PARAM_KEY, QUERY_PARAM_VAL)
                body(TEST_BODY, ErsatzContentType.APPLICATION_JSON.value)
                responder {
                    code HttpStatus.OK.value()
                    content RESPONSE_BODY_JSON, ErsatzContentType.APPLICATION_JSON.value
                }

                called 1
            }
        }

        and: "the appropriate message data"
        String baseUrl = mock.getHttpUrl()
        Map headers = [:]
        headers[TEST_HEADER_KEY] = TEST_HEADER_VAL
        Map queryParams = [:]
        queryParams[QUERY_PARAM_KEY] = QUERY_PARAM_VAL
        String url = "$baseUrl$TEST_URI"

        when: "we send the message"
        HermesResponseWrapper response = FailedMessage.withSession { session ->
            HermesResponseWrapper out = hermesService.makeRequest(Method.PUT, url, ContentType.JSON,
                    null, headers, queryParams, TEST_BODY)
            session.flush()
            return out
        }

        then: "it succeeds"
        response.succeeded

        and: "the response data is populated as expected"
        response.headers
        response.payload
        response.payload.foo == 'bar'
        !response.failedMessageId

        and: "the mock server was called as expected"
        mock.verify()

        and: "there is no FailedMessage object in the database for the message"
        def results = FailedMessage.withSession {
            FailedMessage.withCriteria {
                pgJsonHasFieldValue 'messageData', 'url', url
            }
        }
        !results
    }

    void "test failed message - 500 - verify e2e"() {
        given: "a mock server expecting the request we want to make"
        ErsatzServer mock = TestUtils.newErsatzServer()
        mock.expectations {
            get(TEST_URI) {
                header(TEST_HEADER_KEY, TEST_HEADER_VAL)
                query(QUERY_PARAM_KEY, QUERY_PARAM_VAL)
                responder {
                    code HttpStatus.INTERNAL_SERVER_ERROR.value()
                    content RESPONSE_BODY_JSON, ErsatzContentType.APPLICATION_JSON.value
                }

                called 6
            }
        }

        and: "the appropriate message data"
        String baseUrl = mock.getHttpUrl()
        Map headers = [:]
        headers[TEST_HEADER_KEY] = TEST_HEADER_VAL
        Map queryParams = [:]
        queryParams[QUERY_PARAM_KEY] = QUERY_PARAM_VAL
        String url = "$baseUrl$TEST_URI"

        when: "we send the message"
        HermesResponseWrapper response = FailedMessage.withSession { session ->
            HermesResponseWrapper out = hermesService.makeRequest(Method.GET, url, ContentType.JSON,
                    null, headers, queryParams)
            session.flush()
            return out
        }

        then: "it fails"
        response.failed

        and: "the response data is populated as expected"
        response.headers
        response.payload
        response.payload.foo == 'bar'
        response.failedMessageId

        and: "the mock server was called as expected"
        mock.verify()

        and: "a FailedMessage was created in the database"
        FailedMessage msg = FailedMessage.withSession {
            FailedMessage.get(response.failedMessageId)
        }
        msg
        msg.statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()
        msg.messageData.url == "$baseUrl$TEST_URI"
    }

    void "Test failed message - Connection Refused error - verify e2e"() {
        given: "A stopped server"
        ErsatzServer mock = TestUtils.newErsatzServer()
        mock.start()
        String url = mock.httpUrl
        mock.stop()

        when: "we try to send a request to that server"
        HermesResponseWrapper response = FailedMessage.withSession { session ->
            HermesResponseWrapper out = hermesService.makeRequest(Method.GET, url, ContentType.JSON)
            session.flush()
            return out
        }

        then: "it fails with an IOException"
        response
        response.failed
        response.statusCode == HttpStatusUtils.CONNECTION_FAILURE_CODE
        response.payload
        response.payload instanceof String

        and: "a FailedMessage was created in the database"
        FailedMessage message = FailedMessage.withSession {
            FailedMessage.get(response.failedMessageId)
        }
        message
        message.statusCode == response.statusCode
        message.messageData.url == url
    }

}
