package hermes.integration.test.app


class SampleRetryFailedMessageJobService implements HermesRetryFailedMessageJobTrait {

    def triggerJob() {
        retryFailedMessages()
    }
}
