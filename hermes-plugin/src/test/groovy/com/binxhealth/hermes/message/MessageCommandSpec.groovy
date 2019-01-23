package com.binxhealth.hermes.message

import groovyx.net.http.ContentType
import groovyx.net.http.Method
import spock.lang.Specification
import spock.lang.Unroll

class MessageCommandSpec extends Specification {

    @Unroll("test buildUri with expected output #expected")
    void "test buildUri"() {
        given: "a MessageCommand with specific messageData"
        MessageCommand cmd = new MessageCommand()
        cmd.url = url
        cmd.queryParams = queryParams

        when: "it is encoded into a URI"
        String out = cmd.fullUrl

        then: "the result is as expected"
        out == expected

        where:
        url                             | queryParams                           || expected
        'http://example.com'            | null                                  || 'http://example.com'
        'http://example.com?q=val'      | [:]                                   || 'http://example.com?q=val'
        'http://example.com'            | [q: 'val']                            || 'http://example.com?q=val'
    }

    @Unroll("verify url validation is working correctly for url #url")
    void "verify url validation"() {
        given: "a MessageCommand with a given url"
        MessageCommand cmd = new MessageCommand()
        cmd.url = url

        when: "we validate the command"
        cmd.validate()

        then: "the URL validity is calculated as expected"
        cmd.errors.getFieldError('url') as boolean == isInvalid

        where:
        url                                                                 || isInvalid
        'http://localhost:8080'                                             || false
        'https://localhost:8080/bleh'                                       || false
        'http://env-stage-mtl-mock.stage.svc.cluster.local/Notification'    || false
        'https://test.bleh.local/1/blkf'                                    || false
        'http:///sdf.com'                                                   || true
        'http://sdf'                                                        || true
        'sdf.com'                                                           || true
        null                                                                || true
        'http://.local'                                                     || true
    }

    void "test toMap"() {
        given: "a MessageCommand"
        MessageCommand command = new MessageCommand()
        command.httpMethod = Method.GET
        command.contentType = ContentType.JSON
        command.url = "http://www.test.example.com/endpoint/val"
        command.queryParams = [param: "value"]
        command.headers = [header: "data"]

        when: "we convert it to a Map"
        Map map = command.toMap()

        then: "all the data is preserved as expected"
        map.httpMethod == command.httpMethod
        map.url == command.url
        map.contentType == command.contentType
        map.queryParams == command.queryParams
        map.headers == command.headers

        when: "we build a new MessageCommand from the Map"
        MessageCommand newCommand = new MessageCommand(map)

        then: "all the data transfers back smoothly"
        newCommand.httpMethod == command.httpMethod
        newCommand.contentType == command.contentType
        newCommand.queryParams == command.queryParams
        newCommand.headers == command.headers
        newCommand.fullUrl == command.fullUrl
    }
}
