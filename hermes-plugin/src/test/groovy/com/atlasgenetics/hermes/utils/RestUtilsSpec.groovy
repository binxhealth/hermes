package com.atlasgenetics.hermes.utils

import com.atlasgenetics.hermes.message.MessageCommand
import com.stehno.ersatz.Decoders
import com.stehno.ersatz.ErsatzServer
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import spock.lang.Specification
import spock.lang.Unroll

class RestUtilsSpec extends Specification {

    static final String TEST_URI = "/test"
    static final String HEADER_KEY = "Header"
    static final String HEADER_VAL = "headervalue"
    static final String PARAM_KEY = "query"
    static final String PARAM_VAL = "querydata"
    static final Map BODY = [key: "value"]

    @Unroll("test isInvalid with status #httpStatus and expected value #expected")
    void "test isInvalid"() {
        given: "a status code"
        HttpStatus status = httpStatus

        when: "we check its validity"
        boolean out = RestUtils.isInvalid(status)

        then: "the result is as expected"
        out == expected

        where:
        httpStatus                          || expected
        HttpStatus.OK                       || false
        HttpStatus.BAD_REQUEST              || true
        HttpStatus.INTERNAL_SERVER_ERROR    || false
        HttpStatus.NOT_FOUND                || true
        HttpStatus.PERMANENT_REDIRECT       || false
    }

    @Unroll("test isFailed with status #httpStatus and expected value #expected")
    void "test isFailed"() {
        given: "a status code"
        HttpStatus status = httpStatus

        when: "we check if it is a failure"
        boolean out = RestUtils.isFailed(status)

        then: "the result is as expected"
        out == expected

        where:
        httpStatus                          || expected
        HttpStatus.OK                       || false
        HttpStatus.BAD_REQUEST              || true
        HttpStatus.REQUEST_TIMEOUT          || true
        HttpStatus.INTERNAL_SERVER_ERROR    || true
        HttpStatus.NOT_FOUND                || true
        HttpStatus.PERMANENT_REDIRECT       || true
    }

    void "test makeRequest - GET"() {
        given: "a mock server expecting a GET request"
        ErsatzServer mock = new ErsatzServer()
        mock.expectations {
            get(TEST_URI) {
                query(PARAM_KEY, PARAM_VAL)
                header(HEADER_KEY, HEADER_VAL)

                responder {
                    code HttpStatus.OK.value()
                }

                called 1
            }
        }

        and: "a message data object"
        MessageCommand messageData = buildMessageData(HttpMethod.GET, mock.httpUrl)

        when: "the request is sent"
        HttpStatus status = RestUtils.attemptInitialSend(messageData)

        then: "the request succeeds"
        status == HttpStatus.OK
    }

    void "test makeRequest - PUT"() {
        given: "a mock server expecting a PUT request"
        ErsatzServer mock = new ErsatzServer()
        // I can't believe we have to set this...
        mock.decoder('application/json', Decoders.parseJson)
        mock.expectations {
            put(TEST_URI) {
                query PARAM_KEY, PARAM_VAL
                header HEADER_KEY, HEADER_VAL
                body BODY, 'application/json'

                responder {
                    code HttpStatus.OK.value()
                }

                called 1
            }
        }

        and: "a message data object"
        MessageCommand messageData = buildMessageData(HttpMethod.PUT, mock.httpUrl)

        when: "the request is sent"
        HttpStatus status = RestUtils.attemptInitialSend(messageData)

        then: "the request succeeds"
        status == HttpStatus.OK
    }

    void "test makeRequest - POST"() {
        given: "a mock server expecting a POST request"
        ErsatzServer mock = new ErsatzServer()
        mock.decoder('application/json', Decoders.parseJson)
        mock.expectations {
            post(TEST_URI) {
                query PARAM_KEY, PARAM_VAL
                header HEADER_KEY, HEADER_VAL
                body BODY, 'application/json'

                responder {
                    code HttpStatus.OK.value()
                }

                called 1
            }
        }

        and: "a message data object"
        MessageCommand messageData = buildMessageData(HttpMethod.POST, mock.httpUrl)

        when: "the request is sent"
        HttpStatus status = RestUtils.attemptInitialSend(messageData)

        then: "the request succeeds"
        status == HttpStatus.OK
    }

    void "test makeRequest - DELETE"() {
        given: "a mock server expecting a DELETE request"
        ErsatzServer mock = new ErsatzServer()
        mock.expectations {
            delete(TEST_URI) {
                query(PARAM_KEY, PARAM_VAL)
                header(HEADER_KEY, HEADER_VAL)

                responder {
                    code HttpStatus.OK.value()
                }

                called 1
            }
        }

        and: "a message data object"
        MessageCommand messageData = buildMessageData(HttpMethod.DELETE, mock.httpUrl)

        when: "the request is sent"
        HttpStatus status = RestUtils.attemptInitialSend(messageData)

        then: "the request succeeds"
        status == HttpStatus.OK
    }

    void "test makeRequest - HEAD"() {
        given: "a mock server expecting a HEAD request"
        ErsatzServer mock = new ErsatzServer()
        mock.expectations {
            head(TEST_URI) {
                query(PARAM_KEY, PARAM_VAL)
                header(HEADER_KEY, HEADER_VAL)

                responder {
                    code HttpStatus.OK.value()
                }

                called 1
            }
        }

        and: "a message data object"
        MessageCommand messageData = buildMessageData(HttpMethod.HEAD, mock.httpUrl)

        when: "the request is sent"
        HttpStatus status = RestUtils.attemptInitialSend(messageData)

        then: "the request succeeds"
        status == HttpStatus.OK
    }

    private static MessageCommand buildMessageData(HttpMethod method, String baseUrl) {
        def headers = [:]
        headers[HEADER_KEY] = HEADER_VAL
        def params = [:]
        params[PARAM_KEY] = PARAM_VAL

        MessageCommand command = new MessageCommand()
        command.baseUrl = baseUrl
        command.path = TEST_URI
        command.urlParams = null
        command.headers = headers
        command.queryParams = params
        command.body = BODY
        command.httpMethod = method.name()
        return command
    }

}
