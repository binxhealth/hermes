package com.atlasgenetics.hermes.response

class DefaultHermesResponseHandlerService implements ResponseHandler {

    /**
     * By default, Hermes will do nothing with any response data beyond the status code.  Hermes will not archive
     * or process response bodies or headers.
     * @param response
     */
    void handleResponse(HttpResponseWrapper response) {
        // no-op
    }

}
