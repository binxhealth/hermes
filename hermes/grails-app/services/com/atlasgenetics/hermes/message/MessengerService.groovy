package com.atlasgenetics.hermes.message

import org.springframework.http.HttpMethod

class MessengerService {

    def sendMessageService

    /**
     * This is the entry point for Hermes users.  Through this method, you can invoke the message sending and
     * persistence functionality that Hermes supplies.  Please note that Hermes DOES NOT support sending or receiving
     * multipart forms or PATCH requests.
     * @param httpMethod
     * @param url If the URL contains URL parameters, they should be wrapped in curly braces, e.g. http://test.com/foo/{bar}, where bar is the URL parameter
     * @param headers
     * @param urlParams
     * @param queryParams
     * @param body
     * @return true if the message was sent successfully; false if it failed and has been saved for retry later
     */
    boolean makeRequest(HttpMethod httpMethod, String url, Map headers = null, Map urlParams = null,
                        Map queryParams = null, Map body = null) {
        if (httpMethod && url) {
            Map message = [
                    method: httpMethod,
                    url: url,
                    headers: headers,
                    queryParams: queryParams,
                    urlParams: urlParams,
                    body: body
            ]
            return sendMessageService.sendMessage(message)
        } else {
            throw new IllegalArgumentException("Invalid message request: HttpMethod and URL are required.")
        }
    }
}
