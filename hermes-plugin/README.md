# hermes
A Grails plugin providing guaranteed one-time delivery capabilities for HTTP requests.  This plugin does not archive sent
messages or response data beyond the status code for failed messages.

## Configuration

Application properties:

* `com.binxhealth.hermes.retryTimes` - Sets the maximum number of times Hermes should attempt to send a failed
message during the retry process.  Type: `int`, default value: 5
* `com.binxhealth.hermes.retryInterval` - Sets the time in milliseconds that Hermes will wait between attempts at
making a given HTTP request during the retry process.  Type: `Long`, default value: `10000L` (10 seconds)
