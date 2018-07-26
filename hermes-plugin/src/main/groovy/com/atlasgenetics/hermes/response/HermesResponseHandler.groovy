package com.atlasgenetics.hermes.response

import com.atlasgenetics.hermes.message.FailedMessage
import com.atlasgenetics.hermes.message.MessageCommand

interface HermesResponseHandler {

    void handleSuccessResponse(HttpResponseWrapper response, MessageCommand message)
    void handleFailureResponse(HttpResponseWrapper response, MessageCommand message, FailedMessage failedMessageInstance)

}
