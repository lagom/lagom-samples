# Shopping Cart

This sample application demonstrates a simple shopping cart built with Lagom. It contains two services, a shopping cart service, for managing shopping carts, and a inventory service, for tracking inventory.

The **shopping cart** service persists its data to a relational database using Lagom's persistence API and demonstrates how to persist state using Lagom.

The **inventory service** consumes a stream of events published to Kafka by the shopping cart service, and demonstrates how to consume Kafka event streams in Lagom. However, it doesn't persist its state to a database, it just stores it in memory, and this memory is not shared across nodes. Hence, it should not be used as an example of how to persist state in Lagom.

## Requirements

This sample requires Kafka and Postgres as external services. They are pre-configured in `docker-compose.yml` file. Meaning that before running the service, you first need to start the services using the following command:

```bash
cd shopping-cart/shopping-cart-scala
docker-compose up -d
```

Postgres will be available on port `5432` and Kafka on port `9092`.

Of course, local or remote instances for both services will also work. See more details below.

### Postgres configuration

This configuration is only necessary if you are not using the instance provided by `docker-compose.yml`.

First, you need to create the database, the user, and the password yourself. The application expects it to be running on `localhost` on the default port (`5432`), and it expects there to be a database called `shopping_cart`, with a user called `shopping_cart` with password `shopping_cart` that has full access to it. This can be created using the following SQL:

```sql
CREATE DATABASE shopping_cart;
CREATE USER shopping_cart WITH PASSWORD 'shopping_cart';
GRANT ALL PRIVILEGES ON DATABASE shopping_cart TO shopping_cart;
```

### Kafka Server

This configuration is only necessary if you are not using the instance provided by `docker-compose.yml`.

You need to configure the application to connect to an external Kafka server. Follow [Lagom documentation to see how to do that](https://www.lagomframework.com/documentation/latest/scala/KafkaServer.html#Connecting-to-an-external-Kafka-server).

> **Note**: Some of those configurations are already there, so you only need to update them to your correct values.

## Running in dev mode

After setting up all the requirements, to run the application in dev mode, execute the following command:

```bash
sbt runAll
```

## Shopping cart service

The shopping cart service offers four REST endpoints:

* Get the current contents of the shopping cart:

```bash
curl http://localhost:9000/shoppingcart/123
```

* Get a report of the shopping cart creation and checkout dates:

```bash
curl http://localhost:9000/shoppingcart/123/report
```

* Add an item in the shopping cart:

```bash
curl -H "Content-Type: application/json" -d '{"itemId": "456", "quantity": 2}' -X POST http://localhost:9000/shoppingcart/123
```

* Remove an item in the shopping cart:

```bash
curl -X DELETE http://localhost:9000/shoppingcart/123/item/456
```

* Adjust an item's quantity in the shopping cart:

```bash
curl -H "Content-Type: application/json" -X PATCH -d '{"quantity": 2}' http://localhost:9000/shoppingcart/123/item/456
```

* Check out the shopping cart (i.e., complete the transaction)

```bash
curl -X POST http://localhost:9000/shoppingcart/123/checkout
```

For simplicity, no authentication is implemented, shopping cart IDs are arbitrary and whoever makes the request can use whatever ID they want, and item IDs are also arbitrary and trusted. In a real world application, the shopping cart IDs would likely be random UUIDs to ensure uniqueness, and item IDs would be validated against an item database.

When the shopping cart is checked out, an event is published to the Kafka topic called `shopping-cart` by the shopping cart service. Such events look like this:

```json
{
  "id": "123",
  "items": [
    {"itemId": "456", "quantity": 2},
    {"itemId": "789", "quantity": 1}
  ],
  "checkedOut": true
}
```

## Inventory service

The inventory service offers two REST endpoints:

* Get the inventory of an item:

```bash
curl http://localhost:9000/inventory/456
```

* Add to the inventory of an item:

```bash
curl -H "Content-Type: application/json" -d 4 -X POST http://localhost:9000/inventory/456
```

The inventory service consumes the `shopping-cart` topic from Kafka and decrements the inventory according to the events.
