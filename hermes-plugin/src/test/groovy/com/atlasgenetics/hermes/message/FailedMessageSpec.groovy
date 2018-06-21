package com.atlasgenetics.hermes.message

import com.atlasgenetics.hermes.utils.RestUtils
import org.springframework.http.HttpStatus
import spock.lang.Specification
import spock.lang.Unroll

class FailedMessageSpec extends Specification {
    @Unroll("test isInvalid with status #httpStatus and expected value #expected")
    void "test isInvalid"() {
        given: "a FailedMessage with a status code"
        FailedMessage message = new FailedMessage()
        message.statusCode = httpStatus.value()

        when: "we check its validity"
        boolean out = message.invalid

        then: "the result is as expected"
        out == expected

        where:
        httpStatus                          || expected
        HttpStatus.OK                       || false
        HttpStatus.BAD_REQUEST              || true
        HttpStatus.INTERNAL_SERVER_ERROR    || false
        HttpStatus.NOT_FOUND                || true
        HttpStatus.PERMANENT_REDIRECT       || true
    }

    @Unroll("test isFailed with status #httpStatus and expected value #expected")
    void "test isFailed"() {
        given: "a status code"
        FailedMessage message = new FailedMessage()
        message.statusCode = httpStatus.value()

        when: "we check if it is a failure"
        boolean out = message.failure

        then: "the result is as expected"
        out == expected

        where:
        httpStatus                          || expected
        HttpStatus.OK                       || false
        HttpStatus.BAD_REQUEST              || true
        HttpStatus.REQUEST_TIMEOUT          || true
        HttpStatus.INTERNAL_SERVER_ERROR    || true
        HttpStatus.NOT_FOUND                || true
        HttpStatus.PERMANENT_REDIRECT       || true
    }

    @Unroll("test isSucceeded with status #httpStatus and expected value #expected")
    void "test isSucceeded"() {
        given: "a status code"
        FailedMessage message = new FailedMessage()
        message.statusCode = httpStatus.value()

        when: "we check if it is a failure"
        boolean out = message.success

        then: "the result is as expected"
        out == expected

        where:
        httpStatus                          || expected
        HttpStatus.OK                       || true
        HttpStatus.BAD_REQUEST              || false
        HttpStatus.REQUEST_TIMEOUT          || false
        HttpStatus.INTERNAL_SERVER_ERROR    || false
        HttpStatus.NOT_FOUND                || false
        HttpStatus.PERMANENT_REDIRECT       || false
    }
}
