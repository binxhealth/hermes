package com.atlasgenetics.hermes.message

import com.atlasgenetics.hermes.utils.HttpUtils
import net.kaleidos.hibernate.usertype.JsonbMapType

class FailedMessage {

    Map messageData
    Date dateCreated = new Date()
    Date lastUpdated
    int statusCode

    static constraints = {
        messageData nullable: false
    }

    static mapping = {
        messageData type: JsonbMapType
    }

    boolean isInvalid() {
        HttpUtils.isInvalidMessageCode(statusCode)
    }

    boolean isFailed() {
        HttpUtils.isFailureCode(statusCode)
    }

    boolean isSucceeded() {
        HttpUtils.isSuccessCode(statusCode)
    }

    boolean getIsRedirect() {
        HttpUtils.isRedirectCode(statusCode)
    }

}
