# hermes-integration-test-app

The sole purpose of this app is to run integration tests for the Hermes plugin.

## Running the tests

The integration tests require a working Postgres database.  There is a configuration for a Postgres Docker image already available in the project root directory.  Simply run `$ docker-compose up` and let the image boot before running the integration tests.

You may also run the app, which will likewise require a Postgres DB, but the app has no real functionality as it is essentially a vehicle for the test suite.
