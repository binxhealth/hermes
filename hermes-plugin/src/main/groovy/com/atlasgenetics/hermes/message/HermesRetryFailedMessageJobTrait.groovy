package com.atlasgenetics.hermes.message

import org.springframework.beans.factory.annotation.Autowired

/**
 * This trait is provided for convenience.  To schedule a retry job, you may create a Service class implementing
 * this trait and schedule calls to retryFailedMessage() as desired.  You are not required to use this trait, however;
 * if you want to write your own failed message retry job, go right ahead.
 */
trait HermesRetryFailedMessageJobTrait {

    @Autowired
    def failedMessageService
    @Autowired
    def sendMessageService

    void retryFailedMessages() {
        Set<FailedMessage> messagesToRetry = failedMessageService.gatherAndLockFailedMessagesForRetry()
        messagesToRetry.each {
            sendMessageService.retryFailedMessage(it)
        }
    }
}