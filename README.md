# Nabto WebRTC Signaling SDK for Android

This is the Nabto WebRTC Signaling SDK for android. This repository contains the
core signaling library, utils and an example app.

The libraries in this repository can be used together with the Nabto WebRTC
Signaling Service and a WebRTC library. This way it is possible to create an
application which streams video from an IoT device such as a camera.

## Run the example test application:

1.  copy app/options.properties.default to app/options.properties and insert
    real productid, deviceid and shared secret configuration.
2.  run the app.

A test video feed can be started at https://nabto.github.io/nabto-webrtc-sdk-js/



## Build integration tests:

1. run the integration test server from https://github.com/nabto/nabto-webrtc-sdk-js/tree/main/integration_test_server
2. generate openapi test stubs. ./gradlew openApiGenerate this should only be ran if the openapi specification has been updated in the integration test server.

## run integration tests:

1. run the integration test server from above.
2. specify an url where the integration test server is reachable in project.properties.local
3. run ./gradlew core:connectedAndroidTest
