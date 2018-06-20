package com.atlasgenetics.hermes.message

import org.springframework.scheduling.annotation.Scheduled

/**
 * This class defines the scheduled job that automatically retries sending messages on a delay.
 */
class RetryFailedMessageService {

    def failedMessageService
    def sendMessageService

    // TODO make settings configurable
    @Scheduled(fixedDelay = 3600000L)
    void scheduledRetryJob() {
        Set<FailedMessage> messagesToRetry = failedMessageService.gatherAndLockFailedMessagesForRetry()
        messagesToRetry.each {
            sendMessageService.sendMessage(it, true)
        }
    }
}
