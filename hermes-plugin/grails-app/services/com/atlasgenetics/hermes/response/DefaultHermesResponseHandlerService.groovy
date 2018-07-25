package com.atlasgenetics.hermes.response

import com.atlasgenetics.hermes.message.FailedMessage
import com.atlasgenetics.hermes.message.MessageCommand

class DefaultHermesResponseHandlerService implements ResponseHandler {

    /**
     * By default, Hermes will do nothing with any response data beyond the status code.  Hermes will not archive
     * or process response bodies or headers.
     * @param response
     */
    void handleResponse(HttpResponseWrapper response, MessageCommand messageCommand, FailedMessage failedMessage = null) {
        // no-op
    }

}
