package com.binxhealth.hermes.message

import com.binxhealth.hermes.utils.HttpStatusUtils
import grails.gorm.transactions.Transactional
import groovy.time.TimeCategory

/**
 * This service is provided for convenience's sake.  Any health check functionality included in an application that
 * uses Hermes can go through this service to do any necessary querying against the failed_message table so that users
 * need not implement it themselves.
 * @author Maura Warner
 */
@Transactional(readOnly = true)
class FailedMessageMonitorService {

    /**
     * Builds a CriteriaQuery for FailedMessage based on arguments, passed in as a Map for convenience.  All arguments
     * are optional; any parameters left null will be ignored.  If {@param args} is null or no parameters are set, this
     * method will return the entire contents of the failed_message table.
     * @param createdBefore Date
     * @param createdAfter Date
     * @param updatedBefore Date
     * @param updatedAfter Date
     * @param urlRegEx String, must be formatted as a PostgreSQL regular expression
     * @param statusCodes List<Integer>
     * @param httpMethod String, must be in all caps e.g. 'GET', 'POST'
     * @param orderByProp String, name of the property to order the result set by.  Must be a valid property of FailedMessage; cannot be a property of messageData
     * @param ascDesc String, 'asc' or 'desc' (indicating how to order result set); default value is 'desc'
     * @return Set<FailedMessage> query result set
     */
    List<FailedMessage> listFailedMessages(Map args = null) {
        FailedMessage.createCriteria().listDistinct {
            if (args?.createdBefore) lt('dateCreated', args.createdBefore)
            if (args?.createdAfter) gt('dateCreated', args.createdAfter)
            if (args?.updatedBefore) lt('lastUpdated', args.updatedBefore)
            if (args?.updatedAfter) gt('lastUpdated', args.updatedAfter)
            if (args?.urlRegEx) pgJson('messageData', '->>', 'url', 'like', args.urlRegEx)
            if (args?.statusCodes) 'in'('statusCode', args.statusCodes)
            if (args?.httpMethod) pgJson('messageData', '->>', 'httpMethod', '=', args.httpMethod)
            if (args?.orderByProp) order(args.orderByProp, args.ascDesc ?: 'desc')
        } as List<FailedMessage>
    }

    /**
     * Convenience method.  Populate {@param args} to specify additional criteria.
     * @param args
     * @return FailedMessages created over 24 hours ago
     */
    List<FailedMessage> getMessagesMoreThanOneDayOld(Map args = [:]) {
        use(TimeCategory) {
            args.createdBefore = new Date() - 1.day
            return listFailedMessages(args)
        }
    }

    /**
     * Convenience method.  Populate {@param args} to specify additional criteria.
     * @param args
     * @return FailedMessages that failed with a 3xx status code
     */
    List<FailedMessage> getRedirectedMessages(Map args = [:]) {
        return getMessagesWithStatusCodeInRange(300, 399, args)
    }

    /**
     * Convenience method.  Populate {@param args} to specify additional criteria.
     * @param args
     * @return FailedMessages that failed with a 3xx or 4xx status code
     */
    List<FailedMessage> getInvalidMessages(Map args = [:]) {
        return getMessagesWithStatusCodeInRange(300, 499, args)
    }

    /**
     * Convenience method.  Populate {@param args} to specify additional criteria.
     * @param args
     * @return FailedMessages that failed with a 5xx status code or ConnectException
     */
    List<FailedMessage> getValidMessages(Map args = [:]) {
        List<Integer> statusCodes = [HttpStatusUtils.CONNECTION_FAILURE_CODE]
        statusCodes.addAll((500..1000).toList())
        args.statusCodes = statusCodes
        return listFailedMessages(args)
    }

    /**
     * Convenience method.  Populate {@param args} to specify additional criteria.
     * @param args
     * @return FailedMessages that failed with a ConnectException
     */
    List<FailedMessage> getConnectExceptionMessages(Map args = [:]) {
        args.statusCodes = [HttpStatusUtils.CONNECTION_FAILURE_CODE]
        return listFailedMessages(args)
    }

    /**
     * Convenience method.  Populate {@param args} to specify additional criteria.
     * @param args
     * @param lowerBound Greater than or equal to >=
     * @param upperBound Less than or equal to <=
     * @return FailedMessages that failed with status codes in the specified range
     */
    List<FailedMessage> getMessagesWithStatusCodeInRange(int lowerBound, int upperBound, Map args = [:]) {
        if (args == null) args = [:]
        args.statusCodes = (lowerBound..upperBound).toList()
        return listFailedMessages(args)
    }

}
