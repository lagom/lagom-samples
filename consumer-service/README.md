The "consumer-service" examples demonstrate how to create a service in Lagom that only consumes from other services and does not provide any service calls of its own.

For example, a service could subscribe to a Kafka topic using the Message Broker API and redeliver the messages it consumes to other services by making outbound service calls.
