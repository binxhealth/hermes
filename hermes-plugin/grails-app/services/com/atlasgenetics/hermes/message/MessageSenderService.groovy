package com.atlasgenetics.hermes.message

import com.atlasgenetics.hermes.utils.RestUtils
import org.springframework.http.HttpStatus

class MessageSenderService {

    def failedMessageManagerService

    boolean sendMessage(MessageCommand message) {
        HttpStatus status = RestUtils.attemptInitialSend(message)
        if (status.value() >= 300) {
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
            HttpStatus status = RestUtils.retryMessage(command, 5)
            message.statusCode = status.value()
            if (message.isSuccess()) {
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
