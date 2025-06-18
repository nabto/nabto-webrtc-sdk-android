# Nabto Signaling SDK for Android

## Run the example test application:

1.  copy app/options.properties.default to app/options.properties and insert
    real productid, deviceid and shared secret configuration.

2.  run the app.


## Build integration tests:

1. run the integration test server from https://github.com/nabto/nabto-webrtc-sdk-js/tree/main/integration_test_server
2. generate openapi test stubs. ./gradlew core:openApiGenerate this should only be ran if the openapi specification has been updated in the integration test server.
