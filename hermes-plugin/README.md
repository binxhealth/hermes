# hermes
A Grails plugin with guaranteed one-time delivery capabilities for HTTP requests.  This plugin does not archive sent
messages or response data.  It does not support `PATCH` or `INDEX` requests, or multipart forms.  It does not support
SOAP or content types other than JSON.

## Install the plugin in your local Maven repo

From the project root directory:

`$ grails install`

You should now be able to find the plugin in your `~/.m2` directory and install it as a dependency into any of your
local Grails apps.

## Configuration

Application properties:

* `com.atlasgenetics.hermes.retryTimes` - Sets the maximum number of times Hermes should attempt to send a failed
message during the retry process.  Type: `int`, default value: 5
* `com.atlasgenetics.hermes.retryInterval` - Sets the time in milliseconds that Hermes will wait between attempts at
making a given HTTP request during the retry process.  Type: `Long`, default value: `10000L` (10 seconds)
