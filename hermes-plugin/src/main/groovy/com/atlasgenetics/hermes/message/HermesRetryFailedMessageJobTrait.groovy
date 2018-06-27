package com.atlasgenetics.hermes.message

import grails.async.Promises
import grails.async.PromiseList
import groovy.transform.CompileStatic
import groovyx.gpars.GParsExecutorsPool
import groovyx.gpars.GParsPool
import groovyx.gpars.extra166y.ParallelArray
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

    void retryFailedMessagesInParallel() {
        //if (!maxMessagesPerThread && !maxThreads) throw new IllegalArgumentException(
        //        "maxMessagesPerThread and maxThreads cannot both be null")
        Set<FailedMessage> messagesToRetry = failedMessageManagerService.gatherFailedMessagesForRetry()
       /* List<List<FailedMessage>> collatedMessages = messagesToRetry.collate(maxMessagesPerThread ?:
                Math.ceil(messagesToRetry.size() / maxThreads) as Integer)*/
        GParsExecutorsPool.withPool {
            messagesToRetry.eachParallel { FailedMessage msg ->
                messageSenderService.retryFailedMessage(msg)
            }
        }
    }

}