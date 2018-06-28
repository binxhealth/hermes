package com.atlasgenetics.hermes.message

import com.stehno.ersatz.ContentType
import com.stehno.ersatz.ErsatzServer
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import hermes.integration.test.app.utils.TestUtils
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import spock.lang.Specification

@Integration
@Rollback
class MessengerServiceFunctionalSpec extends Specification {

    def messengerService
    def grailsApplication

    static final String QUERY_PARAM_KEY = "q"
    static final String QUERY_PARAM_VAL = "query"
    static final String TEST_URI = "/endpoint"
    static final Map TEST_BODY = [foo: "bar"]
    static final String TEST_HEADER_KEY = "Header"
    static final String TEST_HEADER_VAL = "Data"

    void "test successful message - verify GET e2e"() {
        given: "a mock server expecting the request we want to make"
        ErsatzServer mock = TestUtils.newErsatzServer()
        mock.expectations {
            get(TEST_URI) {
                header(TEST_HEADER_KEY, TEST_HEADER_VAL)
                query(QUERY_PARAM_KEY, QUERY_PARAM_VAL)
                responder {
                    code HttpStatus.OK.value()
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

        when: "we send the message"
        boolean success = FailedMessage.withSession { session ->
            boolean out = messengerService.makeRequest(HttpMethod.GET, "$baseUrl$TEST_URI", headers,
                    queryParams)
            session.flush()
            return out
        }

        then: "it succeeds"
        success

        and: "the mock server was called as expected"
        mock.verify()

        and: "there is no FailedMessage object in the database for the message"
        def results = FailedMessage.withSession {
            FailedMessage.withCriteria {
                pgJsonHasFieldValue 'messageData', 'baseUrl', baseUrl
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
                body(TEST_BODY, ContentType.APPLICATION_JSON.value)
                responder {
                    code HttpStatus.OK.value()
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

        when: "we send the message"
        boolean success = FailedMessage.withSession { session ->
            boolean out = messengerService.makeRequest(HttpMethod.PUT, "$baseUrl$TEST_URI", headers,
                    queryParams, TEST_BODY)
            session.flush()
            return out
        }

        then: "it succeeds"
        success

        and: "the mock server was called as expected"
        mock.verify()

        and: "there is no FailedMessage object in the database for the message"
        def results = FailedMessage.withSession {
            FailedMessage.withCriteria {
                pgJsonHasFieldValue 'messageData', 'baseUrl', baseUrl
            }
        }
        !results
    }

    void "test successful message - verify POST e2e"() {
        given: "a mock server expecting the request we want to make"
        ErsatzServer mock = TestUtils.newErsatzServer()
        mock.expectations {
            post(TEST_URI) {
                header(TEST_HEADER_KEY, TEST_HEADER_VAL)
                query(QUERY_PARAM_KEY, QUERY_PARAM_VAL)
                body(TEST_BODY, ContentType.APPLICATION_JSON.value)
                responder {
                    code HttpStatus.OK.value()
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

        when: "we send the message"
        boolean success = FailedMessage.withSession { session ->
            boolean out = messengerService.makeRequest(HttpMethod.POST, "$baseUrl$TEST_URI", headers,
                    queryParams, TEST_BODY)
            session.flush()
            return out
        }

        then: "it succeeds"
        success

        and: "the mock server was called as expected"
        mock.verify()

        and: "there is no FailedMessage object in the database for the message"
        def results = FailedMessage.withSession {
            FailedMessage.withCriteria {
                pgJsonHasFieldValue 'messageData', 'baseUrl', baseUrl
            }
        }
        !results
    }

    void "test successful message - verify HEAD e2e"() {
        given: "a mock server expecting the request we want to make"
        ErsatzServer mock = TestUtils.newErsatzServer()
        mock.expectations {
            head(TEST_URI) {
                header(TEST_HEADER_KEY, TEST_HEADER_VAL)
                query(QUERY_PARAM_KEY, QUERY_PARAM_VAL)
                responder {
                    code HttpStatus.OK.value()
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

        when: "we send the message"
        boolean success = FailedMessage.withSession { session ->
            boolean out = messengerService.makeRequest(HttpMethod.HEAD, "$baseUrl$TEST_URI", headers,
                    queryParams)
            session.flush()
            return out
        }

        then: "it succeeds"
        success

        and: "the mock server was called as expected"
        mock.verify()

        and: "there is no FailedMessage object in the database for the message"
        def results = FailedMessage.withSession {
            FailedMessage.withCriteria {
                pgJsonHasFieldValue 'messageData', 'baseUrl', baseUrl
            }
        }
        !results
    }

    void "test successful message - verify DELETE e2e"() {
        given: "a mock server expecting the request we want to make"
        ErsatzServer mock = TestUtils.newErsatzServer()
        mock.expectations {
            delete(TEST_URI) {
                header(TEST_HEADER_KEY, TEST_HEADER_VAL)
                query(QUERY_PARAM_KEY, QUERY_PARAM_VAL)
                responder {
                    code HttpStatus.OK.value()
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

        when: "we send the message"
        boolean success = FailedMessage.withSession { session ->
            boolean out = messengerService.makeRequest(HttpMethod.DELETE, "$baseUrl$TEST_URI", headers,
                    queryParams)
            session.flush()
            return out
        }

        then: "it succeeds"
        success

        and: "the mock server was called as expected"
        mock.verify()

        and: "there is no FailedMessage object in the database for the message"
        def results = FailedMessage.withSession {
            FailedMessage.withCriteria {
                pgJsonHasFieldValue 'messageData', 'baseUrl', baseUrl
            }
        }
        !results
    }

    void "test failed message - verify e2e"() {
        given: "a mock server expecting the request we want to make"
        ErsatzServer mock = TestUtils.newErsatzServer()
        mock.expectations {
            get(TEST_URI) {
                header(TEST_HEADER_KEY, TEST_HEADER_VAL)
                query(QUERY_PARAM_KEY, QUERY_PARAM_VAL)
                responder {
                    code HttpStatus.INTERNAL_SERVER_ERROR.value()
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

        and: "override the retryInterval default so the test runs faster"
        grailsApplication.config.com.atlasgenetics.hermes.retryTimes = 5
        grailsApplication.config.com.atlasgenetics.hermes.retryInterval = 0L

        when: "we send the message"
        boolean success = FailedMessage.withSession { session ->
            boolean out = messengerService.makeRequest(HttpMethod.GET, "$baseUrl$TEST_URI", headers,
                    queryParams)
            session.flush()
            return out
        }

        then: "it fails"
        !success

        and: "the mock server was called as expected"
        mock.verify()

        and: "a FailedMessage was created in the database"
        def results = FailedMessage.withSession {
            FailedMessage.withCriteria {
                pgJsonHasFieldValue 'messageData', 'url', "$baseUrl$TEST_URI"
            }
        }
        results
        results.size() == 1
        def msg = results.first()
        msg
        msg.statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()
    }

}
