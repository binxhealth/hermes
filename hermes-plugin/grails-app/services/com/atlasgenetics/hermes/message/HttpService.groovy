package com.atlasgenetics.hermes.message

import com.atlasgenetics.hermes.response.HermesResponseWrapper
import com.atlasgenetics.hermes.utils.HttpStatusUtils
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseDecorator
import org.apache.http.client.HttpClient
import org.apache.http.client.config.RequestConfig
import org.apache.http.config.SocketConfig
import org.apache.http.impl.client.HttpClients
import javax.annotation.PostConstruct

class HttpService {

    def grailsApplication

    private HTTPBuilder http
    private Long retryInterval
    private Integer maxRetryAttempts
    private Integer requestTimeout

    @PostConstruct
    void init() {
        retryInterval = grailsApplication.config.getProperty('com.atlasgenetics.hermes.retryInterval', Long, 10000L)
        maxRetryAttempts =  grailsApplication.config.getProperty('com.atlasgenetics.hermes.maxRetryAttempts', Integer, 5)
        requestTimeout = grailsApplication.config.getProperty('com.atlasgenetics.hermes.requestTimeout', Integer, 5000)

        SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(requestTimeout).build()
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(requestTimeout).
                setSocketTimeout(requestTimeout).build()
        HttpClient clientConfig = HttpClients.custom().setDefaultSocketConfig(socketConfig).
                setDefaultRequestConfig(requestConfig).build()
        http = new HTTPBuilder()
        http.client = clientConfig
    }

    /**
     * Makes an HTTP request.
     * @param messageData Everything needed to build the request
     * @return response
     */
    HermesResponseWrapper makeRequest(MessageCommand messageData) {
        try {
            HermesResponseWrapper responseWrapper = new HermesResponseWrapper()
            http.request(messageData.fullUrl, messageData.httpMethod, messageData.contentType) {
                if (messageData.headers) headers = messageData.headers
                if (messageData.body) body = messageData.body

                response.success = { HttpResponseDecorator resp, def payload ->
                    populateResponseWrapper(responseWrapper, resp, payload)
                }

                response.failure = { HttpResponseDecorator resp, def payload ->
                    populateResponseWrapper(responseWrapper, resp, payload)
                }
            }
            return responseWrapper
        } catch (IOException e) {
            return new HermesResponseWrapper(
                    statusCode: HttpStatusUtils.CONNECTION_FAILURE_CODE,
                    payload: e
            )
        }
    }

    /**
     * Retries a failed HTTP request up to a given number of times.
     * @param command The failed message
     * @param latestStatusCode The status code with which the message most recently failed
     * @return the last response received during the retry process
     */
    HermesResponseWrapper retryMessage(MessageCommand command, int latestStatusCode) {
        HermesResponseWrapper latestResponse = new HermesResponseWrapper(statusCode: latestStatusCode)
        int attempts = 0
        while (latestResponse.failed && !latestResponse.invalid && attempts < maxRetryAttempts) {
            sleep(retryInterval)
            latestResponse = makeRequest(command)
            attempts++
        }
        return latestResponse
    }

    private static void populateResponseWrapper(HermesResponseWrapper wrapper, HttpResponseDecorator httpResponse,
                                                def payload) {
        wrapper.statusCode = httpResponse.status
        wrapper.headers = httpResponse.allHeaders
        wrapper.payload = payload
    }
}
