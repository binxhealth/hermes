package com.atlasgenetics.hermes.message

import com.atlasgenetics.hermes.utils.RestUtils

class MessageSenderService {

    def failedMessageManagerService

    boolean sendMessage(MessageCommand message) {
        int status = RestUtils.attemptInitialSend(message)
        if (RestUtils.isFailureCode(status)) {
            FailedMessage failedMessage = failedMessageManagerService.createFailedMessage(message, status)
            return retryFailedMessage(failedMessage, message)
        }
        return true
    }

    boolean retryFailedMessage(FailedMessage message, MessageCommand command = null) {
        if (!command) {
            command = new MessageCommand(message.messageData)
        }
        if (!message.invalid) {
            // TODO make retry times configurable
            int statusCode = RestUtils.retryMessage(command, 5, message.statusCode)
            message.statusCode = statusCode
            if (message.succeeded) {
                failedMessageManagerService.purgeMessage(message)
                return true
            } else {
                failedMessageManagerService.unlockMessage(message)
                return false
            }
        } else {
            return false
        }
    }

}
