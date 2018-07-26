package com.atlasgenetics.hermes.response

import com.atlasgenetics.hermes.message.FailedMessage
import com.atlasgenetics.hermes.message.MessageCommand
import org.springframework.stereotype.Component

/**
 * This is the default class provided by Hermes to be registered as the hermesResponseHandler bean.  By default,
 * Hermes will not process or persist any response data besides the status code.  Hermes users who wish to capture
 * response data and do things with it should write their own HermesResponseHandler implementation and register it
 * as the hermesResponseHandler bean in their application.
 *
 * @author Maura Warner
 */
@Component(value = 'hermesResponseHandler')
class DefaultHermesResponseHandler implements HermesResponseHandler {

    void handleSuccessResponse(HttpResponseWrapper response, MessageCommand messageCommand) {
        // no-op
    }

    void handleFailureResponse(HttpResponseWrapper response, MessageCommand messageCommand, FailedMessage failedMessage) {
        // no-op
    }

}
