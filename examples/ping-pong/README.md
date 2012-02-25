# Ping-Pong Example

Demonstrates the use of the EventHandler interface, as well as, how to publish events.  Developers
should get a pretty good idea of how simple it is to create a client-server architecture on the 
Event Bus, using Event Types as the routing mechanism.

## Description ##

In this example, we have created two [EventHandler][1]s, 
one that handles [Ping][2] events and the other that 
handles [Pong][3] events.

The [PingService][4] subscribes to [Ping][2] events.  When a [Ping][2]
is received, the [PingService][4] publishes a [Pong][3] message onto the
bus.

The [PongService][4] subscribes to [Pong][3] events, publishing a [Ping][2]
event.

Both services will print the contents of the received event ([Ping][2] or [Pong][3])
when they are received.  This contents include the timespan between Ping and Pong events
which can be a simple metric indicating how latent message transfers are between
publish and subscriber, including serialization.

## Running the Example ##

To run the example, you will need to have and active Event Bus (RabbitMQ and the
Global Topology Service).  You will also need to start the following services
(with the PingInitiator started last):

- [PingService][4]
- [PongService][5]
- [PingInitiator][6]

The [PingInitiator][6] is used to ignite the Ping-Pong scenario.  We didn't want
a race condition to occur by requiring one of the other services to initiate a 
[Ping][2] or [Pong][3].

[1]: https://github.com/Berico-Technologies/Event-Bus/blob/master/event-bus-client/src/main/java/pegasus/eventbus/client/EventHandler.java
[2]: https://github.com/Berico-Technologies/Event-Bus/blob/master/examples/ping-pong/src/main/java/pegasus/eventbus/examples/pingpong/Ping.java
[3]: https://github.com/Berico-Technologies/Event-Bus/blob/master/examples/ping-pong/src/main/java/pegasus/eventbus/examples/pingpong/Pong.java
[4]: https://github.com/Berico-Technologies/Event-Bus/blob/master/examples/ping-pong/src/main/java/pegasus/eventbus/examples/pingpong/PingService.java
[5]: https://github.com/Berico-Technologies/Event-Bus/blob/master/examples/ping-pong/src/main/java/pegasus/eventbus/examples/pingpong/PongService.java
[6]: https://github.com/Berico-Technologies/Event-Bus/blob/master/examples/ping-pong/src/main/java/pegasus/eventbus/examples/pingpong/PingInitiator.java