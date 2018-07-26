package com.atlasgenetics.hermes.message

import grails.validation.Validateable
import groovyx.net.http.ContentType
import groovyx.net.http.Method
import groovyx.net.http.URIBuilder

/**
 * This Command object provides validation for message data, as well as some convenient methods.
 */
class MessageCommand implements Validateable {

    String url
    Method httpMethod
    ContentType contentType
    Map<String, Object> headers
    Map<String, Object> queryParams
    Map<String, Object> body
    Map<String, Object> metadata

    static constraints = {
        url nullable: false, url: ['localhost:\\d*']
        httpMethod nullable: false
        contentType nullable: false
        headers nullable: true
        queryParams nullable: true
        body nullable: true
        metadata nullable: true
    }

    /**
     * Helper method; puts the message data into a format that can be easily serialized into JSONB data for Postgres.
     * @return MessageCommand data as Map
     */
    Map toMap() {
        [
                url: url,
                httpMethod: httpMethod,
                contentType: contentType,
                headers: headers,
                queryParams: queryParams,
                body: body,
                metadata: metadata
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
