package com.atlasgenetics.hermes.utils

import com.atlasgenetics.hermes.message.MessageCommand
import com.stehno.ersatz.ContentType
import com.stehno.ersatz.Decoders
import com.stehno.ersatz.ErsatzServer
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import spock.lang.Specification
import spock.lang.Unroll

class RestUtilsSpec extends Specification {

    static final String TEST_URI = "/test/foo"
    static final String HEADER_KEY = "Header"
    static final String HEADER_VAL = "headervalue"
    static final String QUERY_KEY = "query"
    static final String QUERY_VAL = "querydata"
    static final Map BODY = [key: "value"]

    @Unroll("test isInvalid with status #httpStatus and expected value #expected")
    void "test isInvalid"() {
        when: "we check a code for validity"
        boolean out = RestUtils.isInvalidMessageCode(httpStatus.value())

        then: "the result is as expected"
        out == expected

        where:
        httpStatus                          || expected
        HttpStatus.OK                       || false
        HttpStatus.BAD_REQUEST              || true
        HttpStatus.INTERNAL_SERVER_ERROR    || false
        HttpStatus.NOT_FOUND                || true
        HttpStatus.PERMANENT_REDIRECT       || true
    }

    @Unroll("test isFailed with status #httpStatus and expected value #expected")
    void "test isFailed"() {
        when: "we check if a code is a failure"
        boolean out = RestUtils.isFailureCode(httpStatus.value())

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

    @Unroll("test isSucceeded with status #httpStatus and expected value #expected")
    void "test isSucceeded"() {
        when: "we check if a code is a success"
        boolean out = RestUtils.isSuccessCode(httpStatus.value())

        then: "the result is as expected"
        out == expected

        where:
        httpStatus                          || expected
        HttpStatus.OK                       || true
        HttpStatus.BAD_REQUEST              || false
        HttpStatus.REQUEST_TIMEOUT          || false
        HttpStatus.INTERNAL_SERVER_ERROR    || false
        HttpStatus.NOT_FOUND                || false
        HttpStatus.PERMANENT_REDIRECT       || false
    }

    void "test makeRequest - GET"() {
        given: "a mock server expecting a GET request"
        ErsatzServer mock = newErsatzServer()
        mock.expectations {
            get(TEST_URI) {
                query(QUERY_KEY, QUERY_VAL)
                header(HEADER_KEY, HEADER_VAL)

                responder {
                    code HttpStatus.OK.value()
                }

                called 1
            }
        }

        and: "a message messageData object"
        MessageCommand messageData = buildMessageData(HttpMethod.GET, mock.httpUrl)

        when: "the request is sent"
        int status = RestUtils.attemptInitialSend(messageData)

        then: "the request succeeds"
        status == HttpStatus.OK.value()
    }

    void "test makeRequest - PUT"() {
        given: "a mock server expecting a PUT request"
        ErsatzServer mock = newErsatzServer()
        mock.expectations {
            put(TEST_URI) {
                query QUERY_KEY, QUERY_VAL
                header HEADER_KEY, HEADER_VAL
                body BODY, ContentType.APPLICATION_JSON.value

                responder {
                    code HttpStatus.OK.value()
                }

                called 1
            }
        }

        and: "a message messageData object"
        MessageCommand messageData = buildMessageData(HttpMethod.PUT, mock.httpUrl)

        when: "the request is sent"
        int status = RestUtils.attemptInitialSend(messageData)

        then: "the request succeeds"
        status == HttpStatus.OK.value()
    }

    void "test makeRequest - POST"() {
        given: "a mock server expecting a POST request"
        ErsatzServer mock = newErsatzServer()
        mock.expectations {
            post(TEST_URI) {
                query QUERY_KEY, QUERY_VAL
                header HEADER_KEY, HEADER_VAL
                body BODY, ContentType.APPLICATION_JSON.value

                responder {
                    code HttpStatus.OK.value()
                }

                called 1
            }
        }

        and: "a message messageData object"
        MessageCommand messageData = buildMessageData(HttpMethod.POST, mock.httpUrl)

        when: "the request is sent"
        int status = RestUtils.attemptInitialSend(messageData)

        then: "the request succeeds"
        status == HttpStatus.OK.value()
    }

    void "test makeRequest - DELETE"() {
        given: "a mock server expecting a DELETE request"
        ErsatzServer mock = newErsatzServer()
        mock.expectations {
            delete(TEST_URI) {
                query(QUERY_KEY, QUERY_VAL)
                header(HEADER_KEY, HEADER_VAL)

                responder {
                    code HttpStatus.OK.value()
                }

                called 1
            }
        }

        and: "a message messageData object"
        MessageCommand messageData = buildMessageData(HttpMethod.DELETE, mock.httpUrl)

        when: "the request is sent"
        int status = RestUtils.attemptInitialSend(messageData)

        then: "the request succeeds"
        status == HttpStatus.OK.value()
    }

    void "test makeRequest - HEAD"() {
        given: "a mock server expecting a HEAD request"
        ErsatzServer mock = newErsatzServer()
        mock.expectations {
            head(TEST_URI) {
                query(QUERY_KEY, QUERY_VAL)
                header(HEADER_KEY, HEADER_VAL)

                responder {
                    code HttpStatus.OK.value()
                }

                called 1
            }
        }

        and: "a message messageData object"
        MessageCommand messageData = buildMessageData(HttpMethod.HEAD, mock.httpUrl)

        when: "the request is sent"
        int status = RestUtils.attemptInitialSend(messageData)

        then: "the request succeeds"
        status == HttpStatus.OK.value()
    }

    private static MessageCommand buildMessageData(HttpMethod method, String baseUrl) {
        def headers = [:]
        headers[HEADER_KEY] = HEADER_VAL
        def queryParams = [:]
        queryParams[QUERY_KEY] = QUERY_VAL

        MessageCommand command = new MessageCommand()
        command.url = "$baseUrl$TEST_URI"
        command.headers = headers
        command.queryParams = queryParams
        command.body = BODY
        command.httpMethod = method.name()
        return command
    }

    /**
     * Ersatz provides a default application/json decoder, but does not associate the decoder with the 'application/json'
     * content type by default (for some reason...), so we have to do it manually.
     * @return new ErsatzServer instance with the application/json decoder appropriately mapped
     */
    private static ErsatzServer newErsatzServer() {
        ErsatzServer mock = new ErsatzServer()
        mock.decoder(ContentType.APPLICATION_JSON.value, Decoders.parseJson)
        return mock
    }

}
