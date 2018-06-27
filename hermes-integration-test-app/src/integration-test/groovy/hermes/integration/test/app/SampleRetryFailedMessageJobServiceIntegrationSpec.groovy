package hermes.integration.test.app

import com.atlasgenetics.hermes.message.FailedMessage
import com.atlasgenetics.hermes.message.MessageCommand
import com.stehno.ersatz.ErsatzServer
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import hermes.integration.test.app.utils.TestUtils
import org.hibernate.engine.spi.Status
import org.junit.internal.runners.statements.Fail
import org.springframework.context.MessageSource
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import spock.lang.Specification

@Integration
@Rollback
class SampleRetryFailedMessageJobServiceIntegrationSpec extends Specification {

    def sampleRetryFailedMessageJobService

    static final String TEST_URI = "/endpoint"

   /* void "test single-thread retry job"() {
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
        cmd.httpMethod = HttpMethod.GET
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
    }*/

    void "test multithreaded retry job"() {
        given: "a mock server expecting certain requests"
        String uri1 = "$TEST_URI${UUID.randomUUID().toString()}"
        String uri2 = "$TEST_URI${UUID.randomUUID().toString()}"
        String uri3= "$TEST_URI${UUID.randomUUID().toString()}"
        String uri4 = "$TEST_URI${UUID.randomUUID().toString()}"

        ErsatzServer mock = TestUtils.newErsatzServer()
        mock.expectations {
            get(uri1) {
                responder {
                    code HttpStatus.INTERNAL_SERVER_ERROR.value()
                }
                called 5
            }
            get(uri2) {
                responder {
                    code HttpStatus.OK.value()
                }
                called 1
            }
            get(uri3) {
                responder {
                    code HttpStatus.INTERNAL_SERVER_ERROR.value()
                }
                called 5
            }
            get(uri4) {
                responder {
                    code HttpStatus.NOT_FOUND.value()
                }
                called 1
            }
        }

        and: "the appropriate failed message data in the DB"
        String baseUrl = mock.getHttpUrl()
        MessageCommand cmd = new MessageCommand()
        cmd.httpMethod = HttpMethod.GET

        cmd.url = "$baseUrl$uri1".toString()
        FailedMessage msg1 = new FailedMessage()
        msg1.statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value()
        msg1.messageData = cmd.toMap()
        msg1.save()

        cmd.url = "$baseUrl$uri2".toString()
        FailedMessage msg2 = new FailedMessage()
        msg2.statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value()
        msg2.messageData = cmd.toMap()
        msg2.save()

        cmd.url = "$baseUrl$uri3".toString()
        FailedMessage msg3 = new FailedMessage()
        msg3.statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value()
        msg3.messageData = cmd.toMap()
        msg3.save()

        cmd.url = "$baseUrl$uri4".toString()
        FailedMessage msg4 = new FailedMessage()
        msg4.statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value()
        msg4.messageData = cmd.toMap()
        msg4.save(flush: true)

        when: "we trigger the multithreaded retry job"
        sampleRetryFailedMessageJobService.triggerMultithreadedJob()

        then: "the mock is called as expected"
        mock.verify()
    }
}
