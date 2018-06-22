package hermes.integration.test.app

import com.atlasgenetics.hermes.message.HermesRetryFailedMessageJobTrait

class SampleRetryFailedMessageJobService implements HermesRetryFailedMessageJobTrait {

    def triggerJob() {
        retryFailedMessages()
    }
}
