package com.example.shoppingcart.impl;

import akka.actor.typed.Behavior;
import akka.cluster.sharding.typed.javadsl.EntityContext;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.*;
import com.example.shoppingcart.impl.ShoppingCartCommand.Checkout;
import com.example.shoppingcart.impl.ShoppingCartCommand.Get;
import com.example.shoppingcart.impl.ShoppingCartCommand.UpdateItem;
import com.example.shoppingcart.impl.ShoppingCartEvent.CheckedOut;
import com.example.shoppingcart.impl.ShoppingCartEvent.ItemUpdated;
import com.example.shoppingcart.impl.ShoppingCartReply.*;

import java.time.Instant;
import java.util.Set;

/**
 * This is an event sourced entity. It has a state, {@link ShoppingCartState}, which
 * stores the current shopping cart items and whether it's checked out.
 * <p>
 * Event sourced entities are interacted with by sending them commands. This
 * entity supports three commands, an {@link UpdateItem} command, which is used to
 * update the quantity of an item in the cart, a {@link Checkout} command which is
 * used to set checkout the shopping cart, and a {@link Get} command, which is a read
 * only command which returns the current shopping cart state.
 * <p>
 * Commands get translated to events, and it's the events that get persisted by
 * the entity. Each event will have an event handler registered for it, and an
 * event handler simply applies an event to the current state. This will be done
 * when the event is first created, and it will also be done when the entity is
 * loaded from the database - each event will be replayed to recreate the state
 * of the entity.
 * <p>
 * This entity defines two events, the {@link ItemUpdated} event, which is emitted
 * when a {@link UpdateItem} command is received, and a {@link CheckedOut} event, which
 * is emitted when a {@link Checkout} command is received.
 */
public class ShoppingCartEntity extends EventSourcedBehaviorWithEnforcedReplies<ShoppingCartCommand, ShoppingCartEvent, ShoppingCartState> {

    // We need to keep the original business id because that's the one we should
    // use when tagging. If we use the PersistenceId, we will change the tag shard
    private final String businessId;

    ShoppingCartEntity(String businessId) {
        this(businessId, ENTITY_TYPE_KEY.persistenceIdFrom(businessId));
    }

    private ShoppingCartEntity(String businessId, PersistenceId persistenceId) {
        super(persistenceId);
        this.businessId = businessId;
    }

    public static EntityTypeKey<ShoppingCartCommand> ENTITY_TYPE_KEY =
        EntityTypeKey
                .create(ShoppingCartCommand.class, "ShoppingCartEntity")
                .withEntityIdSeparator(""); // <- this is important for LagomJava, separator must be an empty String

    public static ShoppingCartEntity behavior(EntityContext<ShoppingCartCommand> entityContext) {
        return new ShoppingCartEntity(entityContext.getEntityId());
    }

    @Override
    public ShoppingCartState emptyState() {
        return ShoppingCartState.EMPTY;
    }

    @Override
    public CommandHandlerWithReply<ShoppingCartCommand, ShoppingCartEvent, ShoppingCartState> commandHandler() {

        CommandHandlerWithReplyBuilder<ShoppingCartCommand, ShoppingCartEvent, ShoppingCartState> builder = newCommandHandlerWithReplyBuilder();

        // Create a behavior for the open shopping cart state.
        builder.forState(ShoppingCartState::isOpen)
                .onCommand(UpdateItem.class, (state, cmd) -> {

                    if (cmd.getQuantity() < 0) {
                        return Effect()
                                .reply(cmd, new Rejected("Quantity must be greater than zero"));

                    } else if( cmd.getQuantity() == 0 && !state.getItems().containsKey(cmd.getProductId())) {
                        return Effect()
                                .reply(cmd, new Rejected("Cannot delete item that is not already in cart"));

                    }else {
                        return Effect()
                                .persist(new ItemUpdated(businessId, cmd.getProductId(), cmd.getQuantity(), Instant.now()))
                                .thenReply(cmd, __ -> new Accepted());
                    }
                })
                .onCommand(Checkout.class, (state, cmd) -> {
                    if (state.getItems().isEmpty()) {
                        return Effect()
                                .reply(cmd, new Rejected("Cannot checkout empty cart"));

                    } else {
                        return Effect()
                                .persist(new CheckedOut(businessId, Instant.now()))
                                .thenReply(cmd, __ -> new Accepted());
                    }
                });


        // Create a behavior for the checked out state.
        builder.forState(ShoppingCartState::isCheckedOut)
                .onCommand(UpdateItem.class,
                        cmd -> Effect().reply(cmd, new Rejected("Can't update item on already checked out shopping cart")))
                .onCommand(Checkout.class,
                        cmd -> Effect().reply(cmd, new Rejected("Can't checkout on already checked out shopping cart")));


        builder.forAnyState().onCommand(Get.class, (state, cmd) -> Effect().reply(cmd, new CurrentState(state)));

        return builder.build();
    }

    @Override
    public EventHandler<ShoppingCartState, ShoppingCartEvent> eventHandler() {

        return newEventHandlerBuilder().forAnyState()
            .onEvent(ItemUpdated.class, (state, evt) -> state.updateItem(evt.getProductId(), evt.getQuantity()))
            .onEvent(CheckedOut.class, (state, evt) -> state.checkout())
        .build();
    }


    @Override
    public Set<String> tagsFor(ShoppingCartEvent shoppingCartEvent) {
        return LagomTaggerAdapter.adapt(businessId, ShoppingCartEvent.TAG).apply(shoppingCartEvent);
    }
}
