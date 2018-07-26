package com.atlasgenetics.hermes.response

import com.atlasgenetics.hermes.utils.HttpUtils
import groovy.transform.CompileStatic

@CompileStatic
class HttpResponse {
    int statusCode
    Map<String, String> headers
    Map<String, Object> body

    boolean isSucceeded() {
        HttpUtils.isSuccessCode(statusCode)
    }

    boolean isFailed() {
       HttpUtils.isFailureCode(statusCode)
    }

    boolean isInvalid() {
        HttpUtils.isInvalidMessageCode(statusCode)
    }
}
