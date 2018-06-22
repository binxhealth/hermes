package com.atlasgenetics.hermes.message

import org.springframework.http.HttpMethod
import spock.lang.Specification
import spock.lang.Unroll

class MessageCommandSpec extends Specification {

    @Unroll("test buildUri with expected output #expected")
    void "test buildUri"() {
        given: "a MessageCommand with specific messageData"
        MessageCommand cmd = new MessageCommand()
        cmd.baseUrl = url
        cmd.path = path
        cmd.urlParams = urlParams
        cmd.queryParams = queryParams

        when: " is encoded into a URI"
        String out = cmd.builtUrl

        then: "the result is as expected"
        out == expected

        where:
        url                             | path                                      | urlParams                             | queryParams                           || expected
        "http://www.example.com"        | "/endpoint"                               | [:]                                   | [:]                                   || "http://www.example.com/endpoint"
        "http://www.example.com/"       | "endpoint"                                | [:]                                   | [query: "value"]                      || "http://www.example.com/endpoint?query=value"
        "http://www.example.com/"       | "/endpoint"                               | [:]                                   | [query: "value", query2: "value2"]    || "http://www.example.com/endpoint?query=value&query2=value2"
        "http://www.example.com"        | "endpoint"                                | [:]                                   | [query: "value&", query2: null]       || "http://www.example.com/endpoint?query=value%26&query2="
        "www.example.com"               | "/endpoint/{param}"                       | [param: "param1"]                     | [:]                                   || "http://www.example.com/endpoint/param1"
        "www.example.com"               | "/endpoint/{param}/foo/{param2}"          | [param: "param1", param2: "bar"]      | [:]                                   || "http://www.example.com/endpoint/param1/foo/bar"
        "localhost:8080"                | "/endpoint/{param}/foo"                   | [param: null]                         | [query: "value"]                      || "http://localhost:8080/endpoint/null/foo?query=value"
        "http://localhost:8080"         | "/endpoint/{param}/foo/{param2}"          | [param: "param1", param2: "&bar"]     | [query: "value", query2: "^-"]        || "http://localhost:8080/endpoint/param1/foo/%26bar?query=value&query2=%5E-"
    }

    void "test toMap"() {
        given: "a MessageCommand"
        MessageCommand command = new MessageCommand()
        command.httpMethod = HttpMethod.GET
        command.baseUrl = "http://www.test.example.com"
        command.path = "/endpoint/{urlParam}"
        command.queryParams = [param: "value"]
        command.headers = [header: "data"]
        command.urlParams = [urlParam: "val"]

        when: "we convert it to a Map"
        Map map = command.toMap()

        then: "all the data is preserved as expected"
        map.httpMethod == command.httpMethod
        map.baseUrl == command.baseUrl
        map.path == command.path
        map.queryParams == command.queryParams
        map.headers == command.headers
        map.urlParams == command.urlParams

        when: "we build a new MessageCommand from the Map"
        MessageCommand newCommand = new MessageCommand(map)

        then: "all the data transfers back smoothly"
        newCommand.httpMethod == command.httpMethod
        newCommand.baseUrl == command.baseUrl
        newCommand.path == command.path
        newCommand.queryParams == command.queryParams
        newCommand.headers == command.headers
        newCommand.urlParams == command.urlParams
        newCommand.builtUrl == command.builtUrl
    }
}
