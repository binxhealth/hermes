package com.atlasgenetics.hermes.utils

import com.atlasgenetics.hermes.message.MessageCommand
import com.atlasgenetics.hermes.response.HttpResponseWrapper
import groovyx.net.http.HTTPBuilder

/**
 * This class houses the actual logic that builds and sends HTTP requests.
 *
 * HTTP response status handling:
 * - 2xx response codes are treated as successes
 * - 3xx and 4xx response codes are treated as failures indiciating an invalid message; messages that fail with these
 *   codes will not be retried
 * - 5xx response codes are treated as failures, but the message itself is still regarded as valid and eligible for
 *   retry
 * - ConnectExceptions will be treated as 5xx errors for most intents and purposes.  All messages failing with
 *   ConnectExceptions will be saved with status code {@link this.CONNECT_EXCEPTION_CODE} and are eligible for retry
 *
 * @author Maura Warner
 */
class HttpUtils {

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
    static HttpResponseWrapper makeRequest(MessageCommand messageData) {
        try {
            HttpResponseWrapper responseWrapper = new HttpResponseWrapper()
            HTTPBuilder http = new HTTPBuilder()
            http.request(messageData.fullUrl, messageData.httpMethod, messageData.contentType) {
                if (messageData.headers) headers = messageData.headers
                if (messageData.body) body = messageData.body

                responseWrapper.success = { resp, body ->
                    populateResponseWrapper(responseWrapper, resp, body)
                }

                responseWrapper.failure = { resp, body ->
                    populateResponseWrapper(responseWrapper, resp, body)
                }
            }
            return responseWrapper
        } catch (ConnectException e) {
            return new HttpResponseWrapper(statusCode: CONNECT_EXCEPTION_CODE)
        }
    }

    /**
     * Retries a failed HTTP request up to a given number of times.
     * @param command The failed message
     * @param times The desired maximum number of retry attempts
     * @return the HTTP status code of the last response received
     */
    static HttpResponseWrapper retryMessage(MessageCommand command, int latestStatusCode, int times,
                                            Long waitTime) {
        HttpResponseWrapper latestResponse = new HttpResponseWrapper(statusCode: latestStatusCode)
        while (latestResponse.failed && !latestResponse.invalid && times > 0) {
            latestResponse = makeRequest(command)
            times--
            if (times > 0) sleep(waitTime)
        }
        return latestResponse
    }

    static boolean isFailureCode(int statusCode) {
        300 <= statusCode || statusCode == CONNECT_EXCEPTION_CODE
    }

    static boolean isInvalidMessageCode(int statusCode) {
        300 <= statusCode && statusCode < 500
    }

    static boolean isSuccessCode(int statusCode) {
        200 <= statusCode && statusCode < 300
    }

    private static void populateResponseWrapper(HttpResponseWrapper wrapper, def httpResponse, def responseBody) {
        wrapper.statusCode = httpResponse.status
        wrapper.headers = httpResponse.headers
        wrapper.body = responseBody
    }

}
