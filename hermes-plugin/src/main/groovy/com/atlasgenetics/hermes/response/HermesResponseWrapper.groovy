package com.atlasgenetics.hermes.response

import com.atlasgenetics.hermes.utils.HttpStatusUtils
import groovy.transform.CompileStatic
import groovyx.net.http.HttpResponseDecorator
import org.apache.http.Header

class HermesResponseWrapper {
    int statusCode
    Map<String, Object> headers
    def payload
    Long failedMessageId

    void setHeaders(Header[] allHeaders) {
        headers = allHeaders.collectEntries { [it.name, it.value] }
    }

    boolean isSucceeded() {
        HttpStatusUtils.isSuccessCode(statusCode)
    }

    boolean isFailed() {
       HttpStatusUtils.isFailureCode(statusCode)
    }

    boolean isInvalid() {
        HttpStatusUtils.isInvalidMessageCode(statusCode)
    }

    boolean isRedirected() {
        HttpStatusUtils.isRedirectCode(statusCode)
    }
}
