package com.atlasgenetics.hermes.message

import grails.gorm.transactions.Transactional
import groovy.time.TimeCategory

/**
 * This service is provided for convenience's sake.  Any health check functionality included in an application that
 * uses Hermes can go through this service to do any necessary querying against the failed_message table so that users
 * need not implement it themselves.
 * @author Maura Warner
 */
@Transactional
class FailedMessageMonitorService {

    /**
     * Builds a CriteriaQuery for FailedMessage based on arguments, passed in as a Map for convenience.  All arguments
     * are optional; any parameters left null will be ignored.  If {@param args} is null or no parameters are set, this
     * method will return the entire contents of the failed_message table.
     * @param createdBefore Date
     * @param createdAfter Date
     * @param updatedBefore Date
     * @param updatedAfter Date
     * @param urlRegEx String, case-sensitive
     * @param locked Boolean
     * @param statusCodes List<int>
     * @param httpMethods List<String>, must be in all caps e.g. 'GET', 'POST'
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
            if (args?.locked != null) eq('locked', args.locked)
            if (args?.httpMethods) pgJson('messageData', '->>', 'httpMethod', 'in', args.httpMethods)
            if (args?.orderByProp) order(args.orderByProp, args.ascDesc ?: 'desc')
        } as List<FailedMessage>
    }

    /**
     * Convenience method.  Populate {@param args} to specify additional criteria.
     * @param args
     * @return FailedMessages created over 24 hours ago
     */
    List<FailedMessage> getFailedMessagesMoreThanOneDayOld(Map args = [:]) {
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
    List<FailedMessage> getRedirectedMessages(Map args = null) {
        return getAllMessagesWithStatusCodeInRange(args, 300, 400)
    }

    /**
     * Convenience method.  Populate {@param args} to specify additional criteria.
     * @param args
     * @return FailedMessages that failed with a 3xx or 4xx status code
     */
    List<FailedMessage> getInvalidMessages(Map args = null) {
        return getAllMessagesWithStatusCodeInRange(args, 300, 500)
    }

    /**
     * Convenience method.  Populate {@param args} to specify additional criteria.
     * @param args
     * @return FailedMessages that failed with a 5xx status code
     */
    List<FailedMessage> getValidMessages(Map args = null) {
        return getAllMessagesWithStatusCodeInRange(args, 500)
    }

    /**
     * Convenience method.  Populate {@param args} to specify additional criteria.
     * @param args
     * @param greaterThan
     * @param lessThan
     * @return FailedMessages that failed with status codes in the specified range
     */
    List<FailedMessage> getAllMessagesWithStatusCodeInRange(Map args, int greaterThan, int lessThan = 600) {
        if (!args) args = [:]
        args.statusCodes = [greaterThan..lessThan]
        return listFailedMessages(args)
    }

}
