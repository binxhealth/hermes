package com.atlasgenetics.hermes.message

import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import groovy.time.TimeCategory
import spock.lang.Specification

@Integration
@Rollback
class FailedMessageMonitorServiceIntegrationSpec extends Specification {

    FailedMessageMonitorService failedMessageMonitorService

    def setupSpec() {
        FailedMessage.withTransaction {
            use(TimeCategory) {
                new FailedMessage()
            }
        }
    }
}
