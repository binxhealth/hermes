package com.atlasgenetics.hermes.message

import org.springframework.beans.factory.annotation.Autowired

/**
 * This trait is provided for convenience.  To schedule a retry job, you may create a Service class implementing
 * this trait and schedule calls to retryFailedMessage() as desired.  You are not required to use this trait to write
 * a failed message retry job, however.
 *
 * @author Maura Warner
 */
trait HermesRetryFailedMessageJobTrait {

    @Autowired
    FailedMessageManagerService failedMessageManagerService
    @Autowired
    MessageSenderService messageSenderService

    void retryFailedMessages() {
        Set<FailedMessage> messagesToRetry = failedMessageManagerService.gatherFailedMessagesForRetry()
        messagesToRetry.each {
            messageSenderService.retryFailedMessage(it)
        }
    }
}