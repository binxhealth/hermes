package com.atlasgenetics.hermes.utils

import org.springframework.http.HttpStatus
import spock.lang.Specification
import spock.lang.Unroll

class HttpStatusUtilsSpec extends Specification {

    @Unroll("test isInvalid with status #statusCode and expected value #expected")
    void "test isInvalid"() {
        when: "we check a code for validity"
        boolean out = HttpStatusUtils.isInvalidMessageCode(statusCode)

        then: "the result is as expected"
        out == expected

        where:
        statusCode                               || expected
        HttpStatus.OK.value()                    || false
        HttpStatus.BAD_REQUEST.value()           || true
        HttpStatus.INTERNAL_SERVER_ERROR.value() || false
        HttpStatus.NOT_FOUND.value()             || true
        HttpStatus.PERMANENT_REDIRECT.value()    || true
        HttpStatusUtils.CONNECT_EXCEPTION_CODE   || false
    }

    @Unroll("test isFailed with status #statusCode and expected value #expected")
    void "test isFailed"() {
        when: "we check if a code is a failure"
        boolean out = HttpStatusUtils.isFailureCode(statusCode)

        then: "the result is as expected"
        out == expected

        where:
        statusCode                               || expected
        HttpStatus.OK.value()                    || false
        HttpStatus.BAD_REQUEST.value()           || true
        HttpStatus.REQUEST_TIMEOUT.value()       || true
        HttpStatus.INTERNAL_SERVER_ERROR.value() || true
        HttpStatus.NOT_FOUND.value()             || true
        HttpStatus.PERMANENT_REDIRECT.value()    || true
        HttpStatusUtils.CONNECT_EXCEPTION_CODE   || true
    }

    @Unroll("test isSucceeded with status #statusCode and expected value #expected")
    void "test isSucceeded"() {
        when: "we check if a code is a success"
        boolean out = HttpStatusUtils.isSuccessCode(statusCode)

        then: "the result is as expected"
        out == expected

        where:
        statusCode                               || expected
        HttpStatus.OK.value()                    || true
        HttpStatus.BAD_REQUEST.value()           || false
        HttpStatus.REQUEST_TIMEOUT.value()       || false
        HttpStatus.INTERNAL_SERVER_ERROR.value() || false
        HttpStatus.NOT_FOUND.value()             || false
        HttpStatus.PERMANENT_REDIRECT.value()    || false
        HttpStatusUtils.CONNECT_EXCEPTION_CODE   || false
    }
}
