package hermes.integration.test.app

import com.atlasgenetics.hermes.message.FailedMessage
import com.atlasgenetics.hermes.message.MessageCommand
import com.stehno.ersatz.ErsatzServer
import grails.testing.mixin.integration.Integration
import groovyx.net.http.ContentType
import groovyx.net.http.Method
import hermes.integration.test.app.utils.TestUtils
import org.hibernate.engine.spi.Status
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification

@Integration
@Rollback
@Transactional
class SampleRetryFailedMessageJobServiceIntegrationSpec extends Specification {

    def sampleRetryFailedMessageJobService

    static final String TEST_URI = "/endpoint"

    void "test Retry Job Trait"() {
        given: "a mock server expecting a given request"
        ErsatzServer mock = TestUtils.newErsatzServer()
        mock.expectations {
            get(TEST_URI) {
                responder {
                    code HttpStatus.OK.value()
                }
                called 1
            }
        }

        and: "a FailedMessage ready for retry containing the appropriate data"
        MessageCommand cmd = new MessageCommand()
        cmd.httpMethod = Method.GET
        cmd.contentType = ContentType.JSON
        cmd.url = "${mock.httpUrl}$TEST_URI"

        FailedMessage message = new FailedMessage()
        message.messageData = cmd.toMap()
        message.statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value()
        message.save(flush: true)

        when: "we trigger the retry message job"
        sampleRetryFailedMessageJobService.triggerJob()

        then: "the mock server receives the message as expected"
        mock.verify()

        and: "the FailedMessage is purged from the database"
        FailedMessage.withSession { session ->
            session.persistenceContext.getEntry(message).status == Status.DELETED
        }
    }
}
