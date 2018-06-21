package com.atlasgenetics.hermes.utils

import com.atlasgenetics.hermes.message.MessageCommand
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import groovy.transform.CompileStatic
import org.springframework.http.HttpMethod

@CompileStatic
class RestUtils {

    /**
     * Makes an HTTP request.
     * @param message
     * @return the HttpStatus of the response
     */
    static int attemptInitialSend(MessageCommand message) {
        return makeRequest(message)
    }

    /**
     * Retries a failed HTTP request up to a given number of times.
     * @param message The failed message
     * @param times The desired maximum number of retry attempts
     * @return The FailedMessage object.  FailedMessage.succeeded = true if a retry succeeded.
     *         FailedMessage.invalid = true if a retry returned a 4xx error
     */
    static int retryMessage(MessageCommand command, int times, int statusCode) {
        while (isFailureCode(statusCode) && !isInvalidMessageCode(statusCode) && times > 0) {
            statusCode = makeRequest(command)
            times--
        }
        return statusCode
    }

    static boolean isFailureCode(int statusCode) {
        300 <= statusCode
    }

    static boolean isInvalidMessageCode(int statusCode) {
        300 <= statusCode && statusCode < 500
    }

    static boolean isSuccessCode(int statusCode) {
        200 <= statusCode && statusCode < 300
    }

    private static int makeRequest(MessageCommand messageData) {
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

    private static int doPost(MessageCommand messageData) {
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
        return resp.statusCode.value()
    }

    private static int doGet(MessageCommand messageData) {
        RestBuilder rest = new RestBuilder()
        RestResponse resp = rest.get(messageData.builtUrl) {
            Map<String, String> headerData = messageData.headers as Map<String, String>
            headerData?.each { k, v ->
                header(k, v)
            }
        }
        return resp.statusCode.value()
    }

    private static int doPut(MessageCommand messageData) {
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
        return resp.statusCode.value()

    }

    private static int doDelete(MessageCommand messageData) {
        RestBuilder rest = new RestBuilder()
        RestResponse resp = rest.delete(messageData.builtUrl) {
            Map<String, String> headerData = messageData.headers as Map<String, String>
            headerData?.each { k, v ->
                header(k, v)
            }
        }
        return resp.statusCode.value()
    }

    private static int doHead(MessageCommand messageData) {
        RestBuilder rest = new RestBuilder()
        RestResponse resp = rest.head(messageData.builtUrl) {
            Map<String, String> headerData = messageData.headers as Map<String, String>
            headerData?.each { k, v ->
                header(k, v)
            }
        }
        return resp.statusCode.value()
    }

}
