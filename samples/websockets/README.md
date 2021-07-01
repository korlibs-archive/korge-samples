# WebSockets Sample

This is a sample showing how to communicate between 
a [KorIO WebSockets client](https://korlibs.soywiz.com/korio/#websocket-client) 
and a [Ktor WebSockets server](https://ktor.io/docs/websocket.html).

## Server

Please note that the [KorIO WebSockets server](https://korlibs.soywiz.com/korio/#server-http-and-websockets) 
is not functional at this date of 1. Juli 2021.
Therefore, this sample server uses [Ktor](https://ktor.io) as a server and can only be started using the JVM target.

Starting the server: `./gradlew :samples:websockets:server:runJvm`

## Client

Starting the client: `./gradlew :samples:websockets:client:runJvm`

## Authors note

This sample is a quick and dirty solution and should only be used as a starting point.
It does not contain exception handling or best practices.
