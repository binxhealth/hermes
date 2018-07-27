# Hermes

Hermes is a Grails plugin providing guaranteed one-time delivery capabilities for HTTP requests.  This plugin does not
retain response data beyond the status code for failed messages, although it does return response data to the caller on
completion of a message send attempt.  Hermes will only retain failed message data; once a message is successfully sent,
it will be purged from the database.

## Configuration

Application properties:

* `com.atlasgenetics.hermes.maxRetryAttempts` - Sets the maximum number of times Hermes should attempt to send a failed
message during the retry process.  Type: `int`, default value: 5
* `com.atlasgenetics.hermes.retryInterval` - Sets the time in milliseconds that Hermes will wait between attempts at
making a given HTTP request during the retry process.  Type: `Long`, default value: `10000L` (10 seconds)
* `com.atlasgenetics.hermes.requestTimeout` - Sets the request timeout for HTTP requests in milliseconds.  Type:
`Integer`, default value: `5000L` (5 seconds)

## Using Hermes

### How Hermes handles responses

Hermes contains no logic to automatically handle redirects at this time.  Thus, its classification of response status
codes is nonstandard.

**Status code policies:**

* 2xx response codes are treated as successes.
* 3xx and 4xx response codes are treated as failures indicating an invalid message; messages that fail with these
codes are ineligible for retry.
* 5xx response codes are treated as failures, but the message itself is still regarded as valid and eligible for
retry.
* Messages that fail with connection errors (e.g. request timeout, connection refused, etc.) will be treated as 5xx
errors for most intents and purposes.  They will be marked as failed and saved to the database with status code 0,
but are not deemed invalid and will thus be eligible for retry.

Most of the logic enforcing these policies is in `MessageSenderService`, although `HttpService.retryMessage()` will
break out of the retry loop and return if presented with a 3xx or 4xx status code.

### Interacting with Hermes: useful methods and entry points

To make use of the full Hermes stack, call one of the `sendMessage` methods in `HermesService`.  Hermes will consume
your message data and use it to build an HTTP request.  It will then attempt to send the request, and will retry up to
a set number of times if the attempt fails.  If the message cannot be sent by the end of the retry operation, it will
be saved in the database as a `FailedMessage`.  Finally, Hermes will return data from the last HTTP response it received
during the message send process, in the form of a `HermesResponseWrapper` instance.  If the process ended in failure,
Hermes will include the `failedMessageId`, which will allow users to pull the relevant `FailedMessage` instance from
the database if desired.

`FailedMessageMonitorService` is provided for your convenience.  It contains a number of canned queries that Hermes
users may need to run frequently against the `failed_message` table, as well as simple utilities for customizing
those queries.  Hermes users need not use `FailedMessageMonitorService` to query the `failed_message` table,
however.

### Writing retry jobs

Hermes contains no baked-in retry job implementation.  Users who wish to build scheduled or triggerable jobs that poll
the `failed_message` table and retry messages persisted there must write the jobs themselves.

`MessageSenderService.retryMessage()` is the recommended entry point for any retry job.  It will give the full benefit
of Hermes' features, without creating any duplicate `FailedMessage` instances that would be caused by a retry job
calling `HermesService.sendMessage()` or `MessageSenderService.attemptInitialSend()`.

`FailedMessageManagerService` is largely for internal use by Hermes, but I do want to draw your attention to one
method, `gatherFailedMessagesForRetry()`.  It is provided for your convenience should you wish to write a simple retry
job that pulls every eligible message from the database and then runs through all of them.  More complicated queries
can be made through `FailedMessageMonitorService`, or you can write them yourself.

## This repo

This is a multi-project build for testing purposes.  The plugin itself is housed in `hermes-plugin`.  Unit tests are
in `hermes-plugin`; integration tests are in `hermes-integration-test-app`.
