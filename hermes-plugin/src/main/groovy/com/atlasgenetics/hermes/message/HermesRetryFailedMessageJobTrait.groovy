package com.atlasgenetics.hermes.message

import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired

/**
 * This trait is provided for convenience.  To schedule a retry job, you may create a Service class implementing
 * this trait and schedule calls to retryFailedMessage() as desired.  You are not required to use this trait to write
 * a failed message retry job, however.
 *
 * @author Maura Warner
 */
@CompileStatic
trait HermesRetryFailedMessageJobTrait {

    @Autowired
    FailedMessageManagerService failedMessageManagerService
    @Autowired
    MessageSenderService messageSenderService

    @Transactional
    void retryFailedMessages(Integer maxMessagesToRetry = null, boolean usePessimisticLock = false) {
        Set<FailedMessage> messagesToRetry = failedMessageManagerService.gatherAndLockFailedMessagesForRetry(maxMessagesToRetry, usePessimisticLock)
        messagesToRetry.each {
            messageSenderService.retryFailedMessage(it)
        }
    }
}