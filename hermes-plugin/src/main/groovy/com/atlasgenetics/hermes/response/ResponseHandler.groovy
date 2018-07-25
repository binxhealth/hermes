package com.atlasgenetics.hermes.response

import com.atlasgenetics.hermes.message.FailedMessage
import com.atlasgenetics.hermes.message.MessageCommand

interface ResponseHandler {

    void handleResponse(HttpResponseWrapper response, MessageCommand message, FailedMessage failedMessageInstance)

}
