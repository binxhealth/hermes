package com.atlasgenetics.hermes.message

import com.atlasgenetics.hermes.utils.RestUtils
import org.springframework.http.HttpStatus

class SendMessageService {

    def failedMessageService

    /**
     * Attempt to make an HTTP request.  If the request fails, it will be retried a set number of times.  If those attempts
     * fail as well, the message will be saved to the database for retry later as part of the scheduled message retry job.
     * @param message
     * @param isRetry
     * @return true if the message was sent successfully; false if the message has been persisted for later retry
     */
    boolean sendMessage(def message, boolean isRetry = false) {
        Map data
        if (isRetry) {
            data = message.data
        } else {
            data = message
        }
        HttpStatus latestStatus = RestUtils.attemptInitialSend(data)
        if (RestUtils.isFailed(latestStatus)) {
            FailedMessage failedMessage = isRetry ? message as FailedMessage
                    : failedMessageService.createFailedMessage(message as Map)
            failedMessage.invalid = RestUtils.isInvalid(latestStatus)
            // TODO make retry times configurable
            failedMessage = RestUtils.retryMessage(failedMessage, 5)
            if (failedMessage.succeeded) {
                failedMessageService.purgeMessage(failedMessage)
                return true
            } else {
                failedMessageService.unlockMessage(failedMessage)
                return false
            }
        } else if (isRetry) {
            failedMessageService.purgeMessage(message as FailedMessage)
            return true
        }
        return true
    }

}
