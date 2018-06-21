package com.atlasgenetics.hermes.message

import org.springframework.http.HttpMethod

class MessengerService {

    def messageSenderService
    def messageSource

    /**
     * This is the entry point for Hermes users.  Through this method, you can invoke the message sending and
     * persistence functionality that Hermes supplies.  Please note that Hermes DOES NOT support sending or receiving
     * multipart forms or PATCH requests.  Please see {@link MessageCommand} for validation rules.
     * @param httpMethod
     * @param baseUrl This can include the path, or the path can be submitted as a separate parameter.  If the URL
     *        contains URL parameters, they should be wrapped in curly braces, e.g. http://test.com/foo/{bar}, where
     *        bar is the URL parameter
     * @param path (optional) If the URL contains URL parameters, they should be wrapped in curly braces, e.g.
     *        http://test.com/foo/{bar}, where bar is the URL parameter
     * @param headers
     * @param urlParams
     * @param queryParams
     * @param body
     * @return true if the message was sent successfully; false if it failed and has been saved for retry later
     */
    boolean makeRequest(HttpMethod httpMethod, String baseUrl, String path = null, Map<String, Object> headers = null,
                        Map<String, Object> urlParams = null, Map<String, Object> queryParams = null,
                        Map<String, Object> body = null) {
        MessageCommand messageCommand = new MessageCommand()
        messageCommand.httpMethod = httpMethod
        messageCommand.baseUrl = baseUrl
        messageCommand.path = path
        messageCommand.headers = headers
        messageCommand.urlParams = urlParams
        messageCommand.queryParams = queryParams
        messageCommand.body = body
        return makeRequest(messageCommand)
    }

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
