package com.atlasgenetics.hermes.utils

import com.atlasgenetics.hermes.message.MessageCommand
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import groovy.transform.CompileStatic
import org.springframework.http.HttpMethod

/**
 * This class houses the actual logic that builds and sends HTTP requests.
 *
 * HTTP response status handling:
 * - 2xx response codes are treated as successes
 * - 3xx and 4xx response codes are treated as failures indiciating an invalid message; messages that fail with these
 *   codes will not be retried
 * - 5xx response codes are treated as failures, but the message itself is still regarded as valid and eligible for
 *   retry
 * - ConnectExceptions thrown by RestBuilder will be treated as 5xx errors for most intents and purposes.  All messages
 *   failing with ConnectExceptions will be saved with status code {@link this.CONNECT_EXCEPTION_CODE} and are eligible
 *   for retry
 *
 * @author Maura Warner
 */
@CompileStatic
class RestUtils {

    /**
     * If a request fails with a ConnectException (e.g. a 'connection refused' error,) the status code for that
     * message will be set to this value.  All messages saved to the database with this status code have failed
     * with ConnectionExceptions and are eligible for retry.
     */
    static final int CONNECT_EXCEPTION_CODE = 0

    /**
     * Makes an HTTP request.
     * @param message
     * @return the HTTP status code of the response
     */
    static int attemptInitialSend(MessageCommand message) {
        try {
            return makeRequest(message)
        } catch (ConnectException e) {
            return CONNECT_EXCEPTION_CODE
        }
    }

    /**
     * Retries a failed HTTP request up to a given number of times.
     * @param command The failed message
     * @param times The desired maximum number of retry attempts
     * @return the HTTP status code of the last response received
     */
    static int retryMessage(MessageCommand command, int times, Long waitTime, int statusCode) {
        while (isFailureCode(statusCode) && !isInvalidMessageCode(statusCode) && times > 0) {
            statusCode = makeRequest(command)
            times--
            if (times > 0) sleep(waitTime)
        }
        return statusCode
    }

    static boolean isFailureCode(int statusCode) {
        300 <= statusCode || statusCode == 0
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
        RestResponse resp = rest.post(messageData.fullUrl) {
            Map<String, String> headerData = messageData.headers as Map<String, String>
            headerData?.each { k, v ->
                header(k, v)
            }
            json {
                messageData.body
            }
        }
        return resp.status
    }

    private static int doGet(MessageCommand messageData) {
        RestBuilder rest = new RestBuilder()
        RestResponse resp = rest.get(messageData.fullUrl) {
            Map<String, String> headerData = messageData.headers as Map<String, String>
            headerData?.each { k, v ->
                header(k, v)
            }
        }
        return resp.status
    }

    private static int doPut(MessageCommand messageData) {
        RestBuilder rest = new RestBuilder()
        RestResponse resp = rest.put(messageData.fullUrl) {
            Map<String, String> headerData = messageData.headers as Map<String, String>
            headerData?.each { k, v ->
                header(k, v)
            }
            json {
                messageData.body
            }
        }
        return resp.status

    }

    private static int doDelete(MessageCommand messageData) {
        RestBuilder rest = new RestBuilder()
        RestResponse resp = rest.delete(messageData.fullUrl) {
            Map<String, String> headerData = messageData.headers as Map<String, String>
            headerData?.each { k, v ->
                header(k, v)
            }
        }
        return resp.status
    }

    private static int doHead(MessageCommand messageData) {
        RestBuilder rest = new RestBuilder()
        RestResponse resp = rest.head(messageData.fullUrl) {
            Map<String, String> headerData = messageData.headers as Map<String, String>
            headerData?.each { k, v ->
                header(k, v)
            }
        }
        return resp.status
    }

}
