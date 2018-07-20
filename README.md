# Hermes
Hermes is a Grails plugin providing guaranteed one-time delivery capabilities for HTTP requests.  This plugin does not
archive sent messages or response data beyond the status code for failed messages.

## Configuration

Application properties:

* `com.atlasgenetics.hermes.retryTimes` - Sets the maximum number of times Hermes should attempt to send a failed
message during the retry process.  Type: `int`, default value: 5
* `com.atlasgenetics.hermes.retryInterval` - Sets the time in milliseconds that Hermes will wait between attempts at
making a given HTTP request during the retry process.  Type: `Long`, default value: `10000L` (10 seconds)

## This Repo

This is a multi-project build for testing purposes.  The plugin itself is housed in `hermes-plugin`.  Unit tests are
in `hermes-plugin`; integration tests are in `hermes-integration-test-app`.
