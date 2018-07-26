package com.atlasgenetics.hermes.response

import com.atlasgenetics.hermes.utils.HttpStatusUtils
import groovy.transform.CompileStatic

@CompileStatic
class HttpResponse {
    int statusCode
    Map<String, String> headers
    Map<String, Object> body

    boolean isSucceeded() {
        HttpStatusUtils.isSuccessCode(statusCode)
    }

    boolean isFailed() {
       HttpStatusUtils.isFailureCode(statusCode)
    }

    boolean isInvalid() {
        HttpStatusUtils.isInvalidMessageCode(statusCode)
    }

    boolean getIsRedirect() {
        HttpStatusUtils.isRedirectCode(statusCode)
    }
}
