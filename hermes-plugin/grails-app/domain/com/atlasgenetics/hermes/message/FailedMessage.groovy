package com.atlasgenetics.hermes.message

import net.kaleidos.hibernate.usertype.JsonbMapType

class FailedMessage {

    Map messageData
    Date dateCreated
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
        300 <= statusCode && statusCode < 500
    }

    boolean isFailure() {
        300 <= statusCode
    }

    boolean isSuccess() {
        200 <= statusCode && statusCode < 300
    }

}
