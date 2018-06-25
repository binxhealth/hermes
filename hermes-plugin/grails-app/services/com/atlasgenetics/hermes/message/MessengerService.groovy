package com.atlasgenetics.hermes.message

import org.springframework.context.MessageSource
import org.springframework.http.HttpMethod

/**
 * This service is the entry point into Hermes for users who wish to take full advantage of its capabilities.  From
 * here, REST requests may be defined and entered into the system.  Hermes will attempt to make the request, and
 * will save any failed requests for a later retry attempt if retrying the message seems warranted.
 *
 * Hermes will not attempt to resend messages that fail with 3xx or 4xx HTTP status codes, as those statuses indicate
 * that some part of the message itself is invalid.  Hermes will persist such messages with the relevant status code,
 * but will not retry them at a later date.
 *
 * Please note that Hermes DOES NOT support PATCH requests, INDEX requests, or multipart forms.
 *
 * Hermes DOES NOT save any response data beyond the status code.  Response bodies etc. will be ignored and discarded.
 *
 * To configure the maximum number of send attempts Hermes should make when retrying a failed message, set application
 * property com.atlasgenetics.hermes.retryTimes
 *
 * @author Maura Warner
 */
class MessengerService {

    MessageSenderService messageSenderService
    MessageSource messageSource

    /**
     * Entry point for Hermes users.  Through this method, you can invoke the message sending and persistence
     * functionality that Hermes supplies.  Please see {@link MessageCommand} for input validation rules.
     *
     * @param httpMethod
     * @param url This can include the queryParams, or they can be included as a separate Map.  NOTE: If the queryParams
     *        are passed in as a Map, the url param will be put through a URL encoder as part of the queryParam
     *        encoding process
     * @param headers
     * @param queryParams
     * @param body
     * @return true if the message was sent successfully; false if it failed
     */
    boolean makeRequest(HttpMethod httpMethod, String url, Map<String, Object> headers = null,
                        Map<String, Object> queryParams = null, Map<String, Object> body = null) {
        MessageCommand messageCommand = new MessageCommand()
        messageCommand.httpMethod = httpMethod
        messageCommand.url = url
        messageCommand.headers = headers
        messageCommand.queryParams = queryParams
        messageCommand.body = body
        return makeRequest(messageCommand)
    }

    /**
     * Entry point for Hermes users.  Through this method, you can invoke the message sending and persistence
     * functionality that Hermes supplies.  Please see {@link MessageCommand} for input validation rules.
     *
     * @param messageCommand prebuilt MessageCommand object
     * @return true if the message was sent successfully; false if it failed
     */
    boolean makeRequest(MessageCommand messageCommand) {
        messageCommand.validate()
        if (messageCommand.hasErrors()) {
            String errorMsg = messageCommand.errors.allErrors*.collect {
                messageSource.getMessage(it.code, it.arguments, it.defaultMessage, Locale.getDefault())
            }.join('; ')
            throw new IllegalArgumentException(errorMsg)
        } else {
            return messageSenderService.sendMessage(messageCommand)
        }
    }
}
