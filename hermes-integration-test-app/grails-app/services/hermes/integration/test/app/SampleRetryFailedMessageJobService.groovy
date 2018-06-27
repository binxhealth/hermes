package hermes.integration.test.app

import com.atlasgenetics.hermes.message.HermesRetryFailedMessageJobTrait

class SampleRetryFailedMessageJobService implements HermesRetryFailedMessageJobTrait {

    static transactional = false

    def triggerJob() {
        retryFailedMessages()
    }

    def triggerMultithreadedJob() {
        retryFailedMessagesInParallel()
    }
}
