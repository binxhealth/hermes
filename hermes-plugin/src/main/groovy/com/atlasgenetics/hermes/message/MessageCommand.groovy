package com.atlasgenetics.hermes.message

import grails.validation.Validateable
import groovyx.net.http.URIBuilder
import org.springframework.http.HttpMethod

/**
 * This Command object provides validation for message data, as well as some convenient methods.
 */
class MessageCommand implements Validateable {

    String url
    String httpMethod
    Map<String, Object> headers
    Map<String, Object> queryParams
    Map<String, Object> body

    static constraints = {
        url nullable: false, url: ['localhost:\\d*']
        httpMethod nullable: false, inList: [HttpMethod.GET.name(), HttpMethod.PUT.name(), HttpMethod.POST.name(), HttpMethod.DELETE.name(), HttpMethod.HEAD.name()]
        headers nullable: true
        queryParams nullable: true
        body nullable: true
    }

    /**
     * Helper method; puts the message data into a format that can be easily serialized into JSONB data for Postgres.
     * @return MessageCommand data as Map
     */
    Map toMap() {
        [
                url: url,
                httpMethod: httpMethod,
                headers: headers,
                queryParams: queryParams,
                body: body
        ]
    }

    String getFullUrl() {
        if (queryParams) {
            URIBuilder uriBuilder = new URIBuilder(url)
            uriBuilder.addQueryParams(queryParams)
            return uriBuilder.toString()
        } else {
            return url
        }
    }
}
