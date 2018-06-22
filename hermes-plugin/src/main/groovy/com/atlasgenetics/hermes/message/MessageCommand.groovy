package com.atlasgenetics.hermes.message

import grails.validation.Validateable
import groovyx.net.http.URIBuilder
import org.springframework.http.HttpMethod

/**
 * This Command object provides validation for message data, as well as some convenient methods.
 */
class MessageCommand implements Validateable {

    String baseUrl
    String path
    String builtUrl
    String httpMethod
    Map<String, Object> headers
    Map<String, Object> queryParams
    Map<String, Object> urlParams
    Map<String, Object> body

    static constraints = {
        baseUrl blank: false
        path nullable: true
        builtUrl nullable: true
        httpMethod nullable: false, inList: [HttpMethod.GET.name(), HttpMethod.PUT.name(), HttpMethod.POST.name(), HttpMethod.DELETE.name(), HttpMethod.HEAD.name()]
        headers nullable: true
        queryParams nullable: true
        urlParams nullable: true
        body nullable: true
    }

    String getBuiltUrl() {
        if (!builtUrl) {
            buildUrl()
        }
        return builtUrl
    }

    /**
     * Helper method; puts the message data into a format that can be easily serialized into JSONB data for Postgres.
     * @return MessageCommand data as Map
     */
    Map toMap() {
        [
                baseUrl: baseUrl,
                path: path,
                builtUrl: builtUrl,
                httpMethod: httpMethod,
                headers: headers,
                queryParams: queryParams,
                urlParams: urlParams,
                body: body
        ]
    }

    /**
     * Builds a URL, handling any and all URL encoding, and checking for leading or trailing slashes on the baseUrl and
     * path params and concatenating them intelligently.
     * @param baseUrl
     * @param path (optional)
     * @param queryParams (optional)
     * @param urlParams (optional)
     * @return URI with all URL params and query params inserted and encoded appropriately
     */
    private String buildUrl() {
        String url
        if (path) {
            if (baseUrl.endsWith('/') && path.startsWith('/')) {
                url = "$baseUrl${path[1..path.length() - 1]}"
            } else if (baseUrl.endsWith('/') || path.startsWith('/') ) {
                url = "$baseUrl$path"
            } else {
                url = "$baseUrl/$path"
            }
        } else {
            url = baseUrl
        }
        if (!url.startsWith('http://')) url = "http://$url"
        urlParams?.each { k, v ->
            url = url.replace(/{$k}/, URLEncoder.encode("$v", "UTF-8"))
        }
        URIBuilder uriBuilder = new URIBuilder(url)
        if (queryParams) uriBuilder.addQueryParams(queryParams)
        builtUrl = uriBuilder.toString()
    }
}
