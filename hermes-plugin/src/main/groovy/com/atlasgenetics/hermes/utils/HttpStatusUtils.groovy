package com.atlasgenetics.hermes.utils

/**
 * This class houses the logic Hermes uses to check HTTP responses for success or failure.
 *
 * HTTP response status handling:
 * - 2xx response codes are treated as successes
 * - 3xx and 4xx response codes are treated as failures indicating an invalid message; messages that fail with these
 *   codes are ineligible for retry
 * - 5xx response codes are treated as failures, but the message itself is still regarded as valid and eligible for
 *   retry
 * - ConnectExceptions will be treated as 5xx errors for most intents and purposes.  All messages failing with
 *   ConnectExceptions will be saved with status code {@link this.CONNECT_EXCEPTION_CODE} and are eligible for retry
 *
 * @author Maura Warner
 */
class HttpStatusUtils {

    /**
     * If a request fails with a ConnectException (e.g. a 'connection refused' error,) the status code for that
     * message will be set to this value.  All messages saved to the database with this status code have failed
     * with ConnectionExceptions and are eligible for retry.
     */
    static final int CONNECT_EXCEPTION_CODE = 0

    static boolean isFailureCode(int statusCode) {
        300 <= statusCode || statusCode == CONNECT_EXCEPTION_CODE
    }

    static boolean isRedirectCode(int statusCode) {
        300 <= statusCode && statusCode < 400
    }

    static boolean isInvalidMessageCode(int statusCode) {
        300 <= statusCode && statusCode < 500
    }

    static boolean isSuccessCode(int statusCode) {
        200 <= statusCode && statusCode < 300
    }

}
