package hermes.integration.test.app

import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class SampleRetryFailedMessageJobServiceSpec extends Specification implements ServiceUnitTest<SampleRetryFailedMessageJobService>{

    def setup() {
    }

    def cleanup() {
    }

    void "test something"() {
        expect:"fix me"
            true == false
    }
}
