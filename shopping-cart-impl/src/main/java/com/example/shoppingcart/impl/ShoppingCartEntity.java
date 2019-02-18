package com.example.shoppingcart.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import java.util.Optional;

import com.example.shoppingcart.impl.ShoppingCartCommand.UpdateItem;
import com.example.shoppingcart.impl.ShoppingCartCommand.Checkout;
import com.example.shoppingcart.impl.ShoppingCartCommand.Get;
import com.example.shoppingcart.impl.ShoppingCartEvent.ItemUpdated;
import com.example.shoppingcart.impl.ShoppingCartEvent.CheckedOut;

/**
 * This is an event sourced entity. It has a state, {@link ShoppingCartState}, which
 * stores the current shopping cart items and whether it's checked out.
 *
 * Event sourced entities are interacted with by sending them commands. This
 * entity supports three commands, an {@link UpdateItem} command, which is used to
 * update the quantity of an item in the cart, a {@link Checkout} command which is
 * used to set checkout the shopping cart, and a {@link Get} command, which is a read
 * only command which returns the current shopping cart state.
 *
 * Commands get translated to events, and it's the events that get persisted by
 * the entity. Each event will have an event handler registered for it, and an
 * event handler simply applies an event to the current state. This will be done
 * when the event is first created, and it will also be done when the entity is
 * loaded from the database - each event will be replayed to recreate the state
 * of the entity.
 *
 * This entity defines two events, the {@link ItemUpdated} event, which is emitted
 * when a {@link UpdateItem} command is received, and a {@link CheckedOut} event, which
 * is emitted when a {@link Checkout} command is received.
 */
public class ShoppingCartEntity extends PersistentEntity<ShoppingCartCommand, ShoppingCartEvent, ShoppingCartState> {
    /**
     * An entity can define different behaviours for different states, but it will
     * always start with an initial behaviour. This entity only has one behaviour.
     */
    @Override
    public Behavior initialBehavior(Optional<ShoppingCartState> snapshotState) {

        BehaviorBuilder b = newBehaviorBuilder(snapshotState.orElse(ShoppingCartState.EMPTY));

        if (b.getState().isCheckedOut()) {
            return checkedOut(b);
        } else {
            return openShoppingCart(b);
        }
    }

    /**
     * Create a behavior for the open shopping cart state.
     */
    private Behavior openShoppingCart(BehaviorBuilder b) {
        // Command handler for the UpdateItem command
        b.setCommandHandler(UpdateItem.class, (cmd, ctx) -> {
            if (cmd.getQuantity() < 0) {
                ctx.commandFailed(new ShoppingCartException("Quantity must be greater than zero"));
                return ctx.done();
            } else if (cmd.getQuantity() == 0 && !state().getItems().containsKey(cmd.getProductId())) {
                ctx.commandFailed(new ShoppingCartException("Cannot delete item that is not already in cart"));
                return ctx.done();
            } else {
                return ctx.thenPersist(new ItemUpdated(entityId(), cmd.getProductId(), cmd.getQuantity()), e -> ctx.reply(Done.getInstance()));
            }
        });

        // Command handler for the Checkout command
        b.setCommandHandler(Checkout.class, (cmd, ctx) -> {
            if (state().getItems().isEmpty()) {
                ctx.commandFailed(new ShoppingCartException("Cannot checkout empty cart"));
                return ctx.done();
            } else {
                return ctx.thenPersist(new CheckedOut(entityId()), e -> ctx.reply(Done.getInstance()));
            }
        });
        commonHandlers(b);
        return b.build();
    }

    /**
     * Create a behavior for the checked out state.
     */
    private Behavior checkedOut(BehaviorBuilder b) {
        b.setReadOnlyCommandHandler(UpdateItem.class, (cmd, ctx) ->
            ctx.commandFailed(new ShoppingCartException("Can't update item on already checked out shopping cart"))
        );
        b.setReadOnlyCommandHandler(Checkout.class, (cmd, ctx) ->
            ctx.commandFailed(new ShoppingCartException("Can't checkout on already checked out shopping cart"))
        );
        commonHandlers(b);
        return b.build();
    }

    /**
     * Add all the handlers that are shared across all states to the behavior builder.
     */
    private void commonHandlers(BehaviorBuilder b) {
        b.setReadOnlyCommandHandler(Get.class, (cmd, ctx) -> ctx.reply(state()));

        b.setEventHandler(ItemUpdated.class, itemUpdated ->
            state().updateItem(itemUpdated.getProductId(), itemUpdated.getQuantity()));

        b.setEventHandlerChangingBehavior(CheckedOut.class, e ->
            checkedOut(newBehaviorBuilder(state().checkout())));
    }

}
