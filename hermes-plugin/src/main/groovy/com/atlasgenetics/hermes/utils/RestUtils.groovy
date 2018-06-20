package com.atlasgenetics.hermes.utils

import com.atlasgenetics.hermes.message.MessageCommand
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import groovy.transform.CompileStatic
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

@CompileStatic
class RestUtils {

    /**
     * Makes an HTTP request.
     * @param message
     * @return the HttpStatus of the response
     */
    static HttpStatus attemptInitialSend(MessageCommand message) {
        return makeRequest(message)
    }

    /**
     * Retries a failed HTTP request up to a given number of times.
     * @param message The failed message
     * @param times The desired maximum number of retry attempts
     * @return The FailedMessage object.  FailedMessage.succeeded = true if a retry succeeded.
     *         FailedMessage.invalid = true if a retry returned a 4xx error
     */
    static HttpStatus retryMessage(MessageCommand message, int times, HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR) {
        while (!isInvalid(status) && isFailed(status) && times > 0) {
            status = makeRequest(message)
            times--
        }
        return status
    }

    static boolean isInvalid(HttpStatus status) {
        400 <= status.value() && status.value() < 500
    }

    static boolean isFailed(HttpStatus status) {
        300 <= status.value()
    }

    static boolean isSuccess(HttpStatus status) {
        200 <= status.value() && status.value() < 300
    }

    private static HttpStatus makeRequest(MessageCommand messageData) {
        switch (messageData.httpMethod as HttpMethod) {
            case HttpMethod.POST:
                return doPost(messageData)
            case HttpMethod.GET:
                return doGet(messageData)
            case HttpMethod.PUT:
                return doPut(messageData)
            case HttpMethod.DELETE:
                return doDelete(messageData)
            case HttpMethod.HEAD:
                return doHead(messageData)
            default:
                throw new IllegalArgumentException("HTTP method ${messageData.httpMethod} not supported")
        }
    }

    private static HttpStatus doPost(MessageCommand messageData) {
        RestBuilder rest = new RestBuilder()
        RestResponse resp = rest.post(messageData.builtUrl) {
            Map<String, String> headerData = messageData.headers as Map<String, String>
            headerData?.each { k, v ->
                header(k, v)
            }
            json {
                messageData.body
            }
        }
        return resp.statusCode
    }

    private static HttpStatus doGet(MessageCommand messageData) {
        RestBuilder rest = new RestBuilder()
        RestResponse resp = rest.get(messageData.builtUrl) {
            Map<String, String> headerData = messageData.headers as Map<String, String>
            headerData?.each { k, v ->
                header(k, v)
            }
        }
        return resp.statusCode
    }

    private static HttpStatus doPut(MessageCommand messageData) {
        RestBuilder rest = new RestBuilder()
        RestResponse resp = rest.put(messageData.builtUrl) {
            Map<String, String> headerData = messageData.headers as Map<String, String>
            headerData?.each { k, v ->
                header(k, v)
            }
            json {
                messageData.body
            }
        }
        return resp.statusCode

    }

    private static HttpStatus doDelete(MessageCommand messageData) {
        RestBuilder rest = new RestBuilder()
        RestResponse resp = rest.delete(messageData.builtUrl) {
            Map<String, String> headerData = messageData.headers as Map<String, String>
            headerData?.each { k, v ->
                header(k, v)
            }
        }
        return resp.statusCode
    }

    private static HttpStatus doHead(MessageCommand messageData) {
        RestBuilder rest = new RestBuilder()
        RestResponse resp = rest.head(messageData.builtUrl) {
            Map<String, String> headerData = messageData.headers as Map<String, String>
            headerData?.each { k, v ->
                header(k, v)
            }
        }
        return resp.statusCode
    }

}
