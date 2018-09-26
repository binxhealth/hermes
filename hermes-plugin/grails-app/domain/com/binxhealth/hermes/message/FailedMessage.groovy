package com.binxhealth.hermes.message

import com.binxhealth.hermes.utils.HttpStatusUtils
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
        HttpStatusUtils.isInvalidMessageCode(statusCode)
    }

    boolean isFailed() {
        HttpStatusUtils.isFailureCode(statusCode)
    }

    boolean isSucceeded() {
        HttpStatusUtils.isSuccessCode(statusCode)
    }

    boolean getIsRedirect() {
        HttpStatusUtils.isRedirectCode(statusCode)
    }

}
