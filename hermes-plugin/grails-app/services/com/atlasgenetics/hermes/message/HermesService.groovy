package com.atlasgenetics.hermes.message

import com.atlasgenetics.hermes.response.HttpResponse
import grails.gorm.transactions.Transactional
import groovyx.net.http.ContentType
import groovyx.net.http.Method
import org.springframework.context.MessageSource

/**
 * This service is the entry point into Hermes for users who wish to take full advantage of its capabilities.  From
 * here, REST requests may be defined and entered into the system.  Hermes will attempt to make the request, and
 * will save any failed requests for a later retry attempt if retrying the message seems warranted.
 *
 * Hermes will not attempt to resend messages that fail with 3xx or 4xx HTTP status codes, as those statuses indicate
 * that some part of the message itself is invalid.  Hermes will persist such messages with the relevant status code,
 * but will not retry them at a later date.
 *
 * Hermes will catch ConnectExceptions thrown by RestBuilder and treat the messages that triggered the exception the
 * same as any message that fails with a 5xx status.  Messages will be saved to the database with a special status code
 * (see {@link com.atlasgenetics.hermes.utils.HttpUtils}), and will be eligible for retry at a later date.
 *
 * Please note that Hermes DOES NOT support multipart forms.
 *
 * Hermes DOES NOT save any response data beyond the status code.  Response bodies etc. will be ignored and discarded.
 *
 * To configure the maximum number of send attempts Hermes should make when retrying a failed message, set application
 * property com.atlasgenetics.hermes.retryTimes
 *
 * To configure the amount of time Hermes should wait between attempts when retrying a failed message, set application
 * property com.atlasgenetics.hermes.retryWaitTime
 *
 * @author Maura Warner
 */
@Transactional
class HermesService {

    MessageSenderService messageSenderService
    MessageSource messageSource

    /**
     * Entry point for Hermes users.  Through this method, you can invoke the message sending and persistence
     * functionality that Hermes supplies.  Please see {@link MessageCommand} for input validation rules.
     *
     * @param httpMethod
     * @param url This can include the queryParams, or they can be included as a separate Map.  NOTE: If the queryParams
     *        are passed in as a Map, url will be put through a URL encoder as part of the queryParam encoding process
     * @param contentType
     * @param metadata This is a Map that allows users to store specific data about a given HTTP request that they
     *        require, but that Hermes does not, e.g. data required by a custom response handler.  Hermes will retain
     *        the data and persist it in the event that the message fails, but will otherwise ignore it.
     * @param headers
     * @param queryParams
     * @param body
     * @return the last response received while attempting to send the message
     */
    HttpResponse makeRequest(Method httpMethod, String url, ContentType contentType, Map<String, Object> metadata = null,
                             Map<String, Object> headers = null, Map<String, Object> queryParams = null,
                             Map<String, Object> body = null) {
        MessageCommand messageCommand = new MessageCommand()
        messageCommand.httpMethod = httpMethod
        messageCommand.url = url
        messageCommand.contentType = contentType
        messageCommand.headers = headers
        messageCommand.queryParams = queryParams
        messageCommand.body = body
        messageCommand.metadata = metadata
        return makeRequest(messageCommand)
    }

    /**
     * Entry point for Hermes users.  Through this method, you can invoke the message sending and persistence
     * functionality that Hermes supplies.  Please see {@link MessageCommand} for input validation rules.
     *
     * @param messageCommand prebuilt MessageCommand object
     * @return the last response received while attempting to send the message
     */
    HttpResponse makeRequest(MessageCommand messageCommand) {
        messageCommand.validate()
        if (messageCommand.hasErrors()) {
            String errorMsg = messageCommand.errors.allErrors*.collect {
                messageSource.getMessage(it.code, it.arguments, it.defaultMessage, Locale.getDefault())
            }.join('; ')
            throw new IllegalArgumentException(errorMsg)
        } else {
            return messageSenderService.sendNewMessage(messageCommand)
        }
    }
}
