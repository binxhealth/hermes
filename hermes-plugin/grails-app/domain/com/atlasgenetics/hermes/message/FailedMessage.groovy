package com.atlasgenetics.hermes.message

import com.atlasgenetics.hermes.utils.RestUtils
import net.kaleidos.hibernate.usertype.JsonbMapType

class FailedMessage {

    Map messageData
    Date dateCreated = new Date()
    Date lastUpdated
    int statusCode
    boolean locked

    static constraints = {
        messageData nullable: false
    }

    static mapping = {
        messageData type: JsonbMapType
    }

    boolean isInvalid() {
        RestUtils.isInvalidMessageCode(statusCode)
    }

    boolean isFailed() {
        RestUtils.isFailureCode(statusCode)
    }

    boolean isSucceeded() {
        RestUtils.isSuccessCode(statusCode)
    }

}
