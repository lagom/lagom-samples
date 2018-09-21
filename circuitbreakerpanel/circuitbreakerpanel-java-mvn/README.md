# CircuitBreakerPanel recipe for Lagom with Java

CircuitBreakerPanel can be used for with any arbitrary api to apply the circuit breaker pattern to it.
Instead of wasting resources on a particular part of the service that's failing you can use the circuit breaker pattern to
fail immediately in case a certain dependency of 1 or more of your endpoints is down. `CircuitBreakerPanel` allows you to maintain 
a registry of circuitBreakers which you can leverage across your application as necessary.

## Implementation details
The [`circuitbreakerpanel-impl/src/main/resources/application.conf`](circuitbreakerpanel-impl/src/main/resources/application.conf) contains configuration for your circuitbreaker.

This configuration is specified in detail in the [`lagom documentation`](https://www.lagomframework.com/documentation/1.4.x/java/ServiceClients.html#Circuit-Breaker-Configuration)

In this sample we use the `CircuitBreakerPanel` in [`DemoServceimpl`](circuitbreakerpanel-impl/src/main/java/com/lightbend/lagom/recipes/cbpanel/impl/DemoServiceImpl.java)
when invoking our userRepository layer.
In a real scenario this userRepository will be interacting with a persistence layer.

Our service basically has the following 3 scenarios - 
1) If the input lies between 0 to 100 , we return a standard `User` POJO  , its a simple 200 Response.
2) If the input is greater than 100 we throw a `UserNotFoundException`. This is a whitelisted exception as we don't want
 the circuitBreaker to open when this is encountered.
3) If the input is negative, we use this to simulate a scenario where the database is down and hence we open the circuitbreaker 
once we encounter this scenario a certain number of times to avoid cascading failure in our service.


## Testing the recipe

To start the service:

```
mvn lagom:runAll
```
To push the CircuitBreaker into an open state just hit the service with a user id which is negative. 
This is used to simulate a situation where the database that the repository interacts with is down.
e.g.
```
curl http://localhost:9000/user/-1
```
Hitting the service with above 3 times and the circuitbreaker goes into an open state, when this happens you will
see the CircuitBreakerOpen exception which indicates calls are failing fast.

But if you hit the service with a value in greater that 100 , even though the userRepository throws an exception, the failure
won't add towards the circuitbreakers open state. For values greater than 100 we are simulating a scenario where the user
does not exist in the database and hence it would not make sense to open the circuitbreaker for a situation like this.
e.g.
```
curl http://localhost:9000/user/1
```


