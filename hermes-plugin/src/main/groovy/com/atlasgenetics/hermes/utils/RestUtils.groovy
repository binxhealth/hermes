package com.atlasgenetics.hermes.utils

import com.atlasgenetics.hermes.message.FailedMessage
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import groovy.transform.CompileStatic
import groovyx.net.http.URIBuilder
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.util.UriComponentsBuilder

@CompileStatic
class RestUtils {

    /**
     * Makes an HTTP request.
     * The data must be in the following format:
     * [
     *      url: "http://some/host.com/example/uri/{foo}/{foo2}",
     *      queryParams: [param: "value"],
     *      urlParams: [foo: "bar", foo2: 1234],
     *      method: HttpMethod.POST,
     *      body: [baz: "bat"]
     * ]
     * @param message
     * @return the HttpStatus of the response
     */
    static HttpStatus attemptInitialSend(Map message) {
        return makeRequest(message)
    }

    /**
     * Retries a failed HTTP request up to a given number of times.
     * @param message The failed message
     * @param times The desired maximum number of retry attempts
     * @return The FailedMessage object.  FailedMessage.succeeded = true if a retry succeeded.
     *         FailedMessage.invalid = true if a retry returned a 4xx error
     */
    static FailedMessage retryMessage(FailedMessage message, int times) {
        while (!message.succeeded && !message.invalid && times > 0) {
            HttpStatus status = makeRequest(message.data)
            if (status.value() < 300) {
                message.succeeded = true
            } else if (isInvalid(status)) {
                message.invalid = true
            }
            times--
        }
        return message
    }

    /**
     * Builds a URL
     * @param baseUrl
     * @param path
     * @param queryParams
     * @param urlParams
     * @return URI with all URI params and query params added
     */
    static String buildUrl(String baseUrl, String path, Map queryParams, Map urlParams) {
        return buildUrl("$baseUrl$path", queryParams, urlParams)
    }

    /**
     * Builds a URI or URL; host information is not required
     * @param baseUrl
     * @param queryParams
     * @param urlParams
     * @return URI with all URI params and query params added
     */
    static String buildUrl(String baseUrl, Map queryParams, Map urlParams) {
        String url = baseUrl
        urlParams?.each { k, v ->
            url = url.replace(/{$k}/, "$v")
        }
        URIBuilder uriBuilder = new URIBuilder(url)
        if (queryParams) uriBuilder.addQueryParams(queryParams)
        return uriBuilder.toString()
    }

    static boolean isInvalid(HttpStatus status) {
        400 <= status.value() && status.value() < 500
    }

    static boolean isFailed(HttpStatus status) {
        400 <= status.value()
    }

    private static HttpStatus makeRequest(Map messageData) {
        switch (messageData.method as HttpMethod) {
            case HttpMethod.POST:
                return doPost(messageData)
            case HttpMethod.GET:
                return doGet(messageData)
            case HttpMethod.PUT:
                return doPut(messageData)
            case HttpMethod.DELETE:
                return doDelete(messageData)
            case HttpMethod.HEAD:
                return doHead(messageData)
            default:
                throw new IllegalArgumentException("HTTP method ${messageData.method} not supported")
        }
    }

    private static HttpStatus doPost(Map messageData) {
        RestBuilder rest = new RestBuilder()
        RestResponse resp = rest.post(buildUrl(messageData.url as String, messageData.queryParams as Map,
                messageData.urlParams as Map)) {
            Map<String, String> headerData = messageData.headers as Map<String, String>
            headerData?.each { k, v ->
                header(k, v)
            }
            json {
                messageData.body
            }
        }
        return resp.statusCode
    }

    private static HttpStatus doGet(Map messageData) {
        RestBuilder rest = new RestBuilder()
        RestResponse resp = rest.get(buildUrl(messageData.url as String, messageData.queryParams as Map,
                messageData.urlParams as Map)) {
            Map<String, String> headerData = messageData.headers as Map<String, String>
            headerData?.each { k, v ->
                header(k, v)
            }
        }
        return resp.statusCode
    }

    private static HttpStatus doPut(Map messageData) {
        RestBuilder rest = new RestBuilder()
        RestResponse resp = rest.put(buildUrl(messageData.url as String, messageData.queryParams as Map,
                messageData.urlParams as Map)) {
            Map<String, String> headerData = messageData.headers as Map<String, String>
            headerData?.each { k, v ->
                header(k, v)
            }
            json {
                messageData.body
            }
        }
        return resp.statusCode

    }

    private static HttpStatus doDelete(Map messageData) {
        RestBuilder rest = new RestBuilder()
        RestResponse resp = rest.delete(buildUrl(messageData.url as String, messageData.queryParams as Map,
                messageData.urlParams as Map)) {
            Map<String, String> headerData = messageData.headers as Map<String, String>
            headerData?.each { k, v ->
                header(k, v)
            }
        }
        return resp.statusCode
    }

    private static HttpStatus doHead(Map messageData) {
        RestBuilder rest = new RestBuilder()
        RestResponse resp = rest.head(buildUrl(messageData.url as String, messageData.queryParams as Map,
                messageData.urlParams as Map)) {
            Map<String, String> headerData = messageData.headers as Map<String, String>
            headerData?.each { k, v ->
                header(k, v)
            }
        }
        return resp.statusCode
    }

}
