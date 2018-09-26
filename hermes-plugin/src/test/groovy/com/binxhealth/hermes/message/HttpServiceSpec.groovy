package com.binxhealth.hermes.message

import com.binxhealth.hermes.response.HermesResponseWrapper
import com.stehno.ersatz.ContentType as ErsatzContentType
import com.stehno.ersatz.Decoders
import com.stehno.ersatz.ErsatzServer
import grails.testing.services.ServiceUnitTest
import groovyx.net.http.ContentType
import groovyx.net.http.Method
import org.springframework.http.HttpStatus
import spock.lang.Specification

class HttpServiceSpec extends Specification implements ServiceUnitTest<HttpService> {

    static final String TEST_URI = "/test/foo"
    static final String HEADER_KEY = "Header"
    static final String HEADER_VAL = "headervalue"
    static final String QUERY_KEY = "query"
    static final String QUERY_VAL = "querydata"
    static final Map BODY = [key: "value"]
    static final String RESPONSE_BODY_JSON = '{"foo":"bar"}'

    void "test makeRequest - GET"() {
        given: "a mock server expecting a GET request"
        ErsatzServer mock = newErsatzServer()
        mock.expectations {
            get(TEST_URI) {
                query(QUERY_KEY, QUERY_VAL)
                header(HEADER_KEY, HEADER_VAL)

                responder {
                    code HttpStatus.OK.value()
                    content RESPONSE_BODY_JSON, ErsatzContentType.APPLICATION_JSON.value
                }

                called 1
            }
        }

        and: "a message messageData object"
        MessageCommand messageData = buildMessageData(Method.GET, mock.httpUrl, ContentType.JSON)

        when: "the request is sent"
        HermesResponseWrapper response = service.makeRequest(messageData)

        then: "the request succeeds"
        response.statusCode == HttpStatus.OK.value()

        and: "all the response data is populated as expected"
        response.headers
        response.payload
        response.payload.foo == 'bar'

        and: "the mock server was called as expected"
        mock.verify()
    }

    void "test makeRequest - PUT"() {
        given: "a mock server expecting a POST request"
        ErsatzServer mock = newErsatzServer()
        mock.expectations {
            put(TEST_URI) {
                query QUERY_KEY, QUERY_VAL
                header HEADER_KEY, HEADER_VAL
                body BODY, ErsatzContentType.APPLICATION_JSON.value

                responder {
                    code HttpStatus.OK.value()
                    content RESPONSE_BODY_JSON, ErsatzContentType.APPLICATION_JSON.value
                }

                called 1
            }
        }

        and: "a message messageData object"
        MessageCommand messageData = buildMessageData(Method.PUT, mock.httpUrl, ContentType.JSON)

        when: "the request is sent"
        HermesResponseWrapper response = service.makeRequest(messageData)

        then: "the request succeeds"
        response.statusCode == HttpStatus.OK.value()

        and: "all the response data is populated as expected"
        response.headers
        response.payload
        response.payload.foo == 'bar'

        and: "the mock server was called as expected"
        mock.verify()
    }

    private static MessageCommand buildMessageData(Method method, String baseUrl, ContentType contentType) {
        def headers = [:]
        headers[HEADER_KEY] = HEADER_VAL
        def queryParams = [:]
        queryParams[QUERY_KEY] = QUERY_VAL

        MessageCommand command = new MessageCommand()
        command.url = "$baseUrl$TEST_URI"
        command.headers = headers
        command.contentType = contentType
        command.queryParams = queryParams
        if (method in [Method.PUT, Method.POST]) command.body = BODY
        command.httpMethod = method
        return command
    }

    /**
     * Ersatz provides a default application/json decoder, but does not associate the decoder with the 'application/json'
     * content type by default (for some reason...), so we have to do it manually.
     * @return new ErsatzServer instance with the application/json decoder appropriately mapped
     */
    private static ErsatzServer newErsatzServer() {
        ErsatzServer mock = new ErsatzServer()
        mock.decoder(ErsatzContentType.APPLICATION_JSON.value, Decoders.parseJson)
        return mock
    }
}
