package com.example.shoppingcart.impl;

import akka.Done;
import akka.NotUsed;
import akka.japi.Pair;
import com.example.shoppingcart.api.ShoppingCart;
import com.example.shoppingcart.api.ShoppingCartReportView;
import com.example.shoppingcart.api.ShoppingCartService;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.transport.BadRequest;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;

import javax.inject.Inject;

import com.example.shoppingcart.api.ShoppingCartItem;
import org.pcollections.TreePVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * Implementation of the {@link ShoppingCartService}.
 */
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final PersistentEntityRegistry persistentEntityRegistry;

    private final ReportRepository reportRepository;

    @Inject
    public ShoppingCartServiceImpl(PersistentEntityRegistry persistentEntityRegistry,
            ReportRepository reportRepository) {
        this.persistentEntityRegistry = persistentEntityRegistry;
        this.reportRepository = reportRepository;
        persistentEntityRegistry.register(ShoppingCartEntity.class);
    }

    private PersistentEntityRef<ShoppingCartCommand> entityRef(String id) {
        return persistentEntityRegistry.refFor(ShoppingCartEntity.class, id);
    }

    @Override
    public ServiceCall<NotUsed, ShoppingCart> get(String id) {
        return request -> entityRef(id).ask(ShoppingCartCommand.Get.INSTANCE)
                .thenApply(cart -> convertShoppingCart(id, cart));
    }

    @Override
    public ServiceCall<NotUsed, ShoppingCartReportView> getReport(String id) {
        return request -> reportRepository.findById(id).thenApply(report -> {
            if (report != null)
                return new ShoppingCartReportView(id, report.getCreationDate(), report.getCheckoutDate());
            else
                throw new NotFound("Couldn't find a shopping cart report for '" + id + "'");
        });
    }

    @Override
    public ServiceCall<ShoppingCartItem, Done> updateItem(String id) {
        return item -> convertErrors(
                entityRef(id).ask(new ShoppingCartCommand.UpdateItem(item.getProductId(), item.getQuantity())));
    }

    @Override
    public ServiceCall<NotUsed, Done> checkout(String id) {
        return request -> convertErrors(entityRef(id).ask(ShoppingCartCommand.Checkout.INSTANCE));
    }

    @Override
    public Topic<ShoppingCart> shoppingCartTopic() {
        // We want to publish all the shards of the shopping cart events
        return TopicProducer.taggedStreamWithOffset(ShoppingCartEvent.TAG.allTags(), (tag, offset) ->
        // Load the event stream for the passed in shard tag
        persistentEntityRegistry.eventStream(tag, offset)
                // We only want to publish checkout events
                .filter(pair -> pair.first() instanceof ShoppingCartEvent.CheckedOut)
                // Now we want to convert from the persisted event to the published event.
                // To do this, we need to load the current shopping cart state.
                .mapAsync(4, eventAndOffset -> {
                    ShoppingCartEvent.CheckedOut checkedOut = (ShoppingCartEvent.CheckedOut) eventAndOffset.first();
                    return entityRef(checkedOut.getShoppingCartId()).ask(ShoppingCartCommand.Get.INSTANCE)
                            .thenApply(cart -> Pair.create(convertShoppingCart(checkedOut.getShoppingCartId(), cart),
                                    eventAndOffset.second()));
                }));
    }

    private <T> CompletionStage<T> convertErrors(CompletionStage<T> future) {
        return future.exceptionally(ex -> {
            if (ex instanceof ShoppingCartException) {
                throw new BadRequest(ex.getMessage());
            } else {
                throw new BadRequest("Error updating shopping cart");
            }
        });
    }

    private ShoppingCart convertShoppingCart(String id, ShoppingCartState cart) {
        List<ShoppingCartItem> items = new ArrayList<>();
        for (Map.Entry<String, Integer> item : cart.getItems().entrySet()) {
            items.add(new ShoppingCartItem(item.getKey(), item.getValue()));
        }
        return new ShoppingCart(id, items, cart.isCheckedOut());
    }

}
