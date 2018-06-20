package com.atlasgenetics.hermes.message

class FailedMessage {

    MessageCommand data
    Date dateCreated
    Date lastUpdated
    boolean locked
    boolean invalid
    boolean succeeded = false

    static transients = ['succeeded']

    static constraints = {
        data nullable: false
    }

    static mapping = {
        data type: 'jsonb'
    }

}
