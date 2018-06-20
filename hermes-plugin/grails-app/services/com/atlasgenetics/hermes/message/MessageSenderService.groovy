package com.atlasgenetics.hermes.message

import com.atlasgenetics.hermes.utils.RestUtils
import org.springframework.http.HttpStatus

class MessageSenderService {

    def failedMessageManagerService

    boolean sendMessage(MessageCommand message) {
        HttpStatus status = RestUtils.attemptInitialSend(message)
        if (RestUtils.isFailed(status)) {
            FailedMessage failedMessage = failedMessageManagerService.createFailedMessage(message, status)
            return retryFailedMessage(failedMessage)
        }
        return true
    }

    boolean retryFailedMessage(FailedMessage message) {
        if (!message.invalid) {
            // TODO make retry times configurable
            HttpStatus status = RestUtils.retryMessage(message.data, 5)
            if (RestUtils.isSuccess(status)) {
                failedMessageManagerService.purgeMessage(message)
                return true
            } else {
                message.invalid = RestUtils.isInvalid(status)
                failedMessageManagerService.unlockMessage(message)
                return false
            }
        } else {
            return false
        }
    }

}
