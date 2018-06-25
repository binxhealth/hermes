package com.atlasgenetics.hermes.message

import org.springframework.http.HttpMethod
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

    void "test toMap"() {
        given: "a MessageCommand"
        MessageCommand command = new MessageCommand()
        command.httpMethod = HttpMethod.GET
        command.url = "http://www.test.example.com/endpoint/val"
        command.queryParams = [param: "value"]
        command.headers = [header: "data"]

        when: "we convert it to a Map"
        Map map = command.toMap()

        then: "all the data is preserved as expected"
        map.httpMethod == command.httpMethod
        map.url == command.url
        map.queryParams == command.queryParams
        map.headers == command.headers

        when: "we build a new MessageCommand from the Map"
        MessageCommand newCommand = new MessageCommand(map)

        then: "all the data transfers back smoothly"
        newCommand.httpMethod == command.httpMethod
        newCommand.queryParams == command.queryParams
        newCommand.headers == command.headers
        newCommand.fullUrl == command.fullUrl
    }
}
