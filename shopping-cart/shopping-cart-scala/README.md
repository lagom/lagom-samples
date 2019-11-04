# Shopping Cart

This sample application demonstrates a simple shopping cart built with Lagom. It contains two services, a shopping cart service, for managing shopping carts, and an inventory service, for tracking inventory.

The shopping cart service persists its data to a relational database using Lagom's persistence API, and is intended to demonstrate how to persist state using Lagom.

The inventory service consumes a stream of events published to Kafka by the shopping cart service, and is intended to demonstrate how Kafka event streams can be consumed in Lagom. However, it doesn't persist its state to a database, it just stores it in memory, and this memory is not shared across nodes. Hence, it should not be used as an example of how to store state in Lagom.

## Setup

To run this application locally you will need access to a Postgres database. We suggest you run it on a docker container but a local or remote native instance will also work.

We provide a `docker-compose.yml` file that you can use to run a Postgres database already configured for this application. The docker container will be exposed on port 5432.

To create the image and start the container, run the command below at the root of this project.

```bash
docker-compose up -d
```

If you prefer to run Postgres natively on your machine, you need to create the database, the user and password yourself. The application expects it to be running on localhost on the default port (5432), and it expects there to be a database called `shopping_cart`, with a user called `shopping_cart` with password `shopping_cart` that has full access to it. This can be created using the following SQL:

```sql
CREATE DATABASE shopping_cart;
CREATE USER shopping_cart WITH PASSWORD 'shopping_cart';
GRANT ALL PRIVILEGES ON DATABASE shopping_cart TO shopping_cart;
```

Once Postgres is setup, you can start the system by running:

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

* Checkout the shopping cart (ie, complete the transaction)

```bash
curl -X POST http://localhost:9000/shoppingcart/123/checkout
```

For simplicity, no authentication is implemented, shopping cart IDs are arbitrary and whoever makes the request can use whatever ID they want, and item IDs are also arbitrary and trusted. An a real world application, the shopping cart IDs would likely be random UUIDs to ensure uniqueness, and item IDs would be validated against a item database.

When the shopping cart is checked out, an event is published to the Kafka topic called `shopping-cart` by the shopping cart service, such events look like this:

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

The inventory service consumes the `shopping-cart` topic from Kafka, and decrements the inventory according to the events.
