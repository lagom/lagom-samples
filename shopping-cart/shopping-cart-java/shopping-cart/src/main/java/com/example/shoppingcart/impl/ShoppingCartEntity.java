package com.example.shoppingcart.impl;

import akka.actor.typed.ActorRef;
import akka.cluster.sharding.typed.javadsl.EntityContext;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.*;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.javadsl.persistence.AkkaTaggerAdapter;
import com.lightbend.lagom.serialization.CompressedJsonable;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class ShoppingCartEntity extends EventSourcedBehaviorWithEnforcedReplies<ShoppingCartEntity.Command, ShoppingCartEntity.Event, ShoppingCartEntity.ShoppingCart> {

    private final String cartId;
    
    private final Function <Event, Set<String>> tagger;
    
    static EntityTypeKey<Command> ENTITY_TYPE_KEY = EntityTypeKey.create(Command.class, "ShoppingCart");
    
    private ShoppingCartEntity(EntityContext<Command> entityContext) {
        // PersistenceId needs a typeHint (or namespace) and entityId, we take then from the EntityContext
        super(PersistenceId.of(entityContext.getEntityTypeKey().name(), entityContext.getEntityId()));
        // we keep a copy of cartId because it's used in the events
        this.cartId = entityContext.getEntityId();
        // tagger is constructed from adapter and needs EntityContext
        this.tagger = AkkaTaggerAdapter.fromLagom(entityContext, Event.TAG);
    }

    static ShoppingCartEntity create(EntityContext<Command> entityContext) {
        return new ShoppingCartEntity(entityContext);
    }

    //
    // SHOPPING CART COMMANDS
    //
    interface Command<R> extends Jsonable {}

    @Value
    @JsonDeserialize
    static final class AddItem implements Command<Confirmation>, CompressedJsonable {
        public final String itemId;
        public final int quantity;
        public final ActorRef<Confirmation> replyTo;

        @JsonCreator
        AddItem(String itemId, int quantity, ActorRef<Confirmation> replyTo) {
            this.itemId = Preconditions.checkNotNull(itemId, "itemId");
            this.quantity = quantity;
            this.replyTo = replyTo;
        }
    }

    @Value
    @JsonDeserialize
    static final class RemoveItem implements Command<Confirmation> {
        public final String itemId;
        public final ActorRef<Confirmation> replyTo;

        @JsonCreator
        RemoveItem(String itemId, ActorRef<Confirmation> replyTo) {
            this.itemId = Preconditions.checkNotNull(itemId, "itemId");
            this.replyTo = replyTo;
        }
    }

    @Value
    @JsonDeserialize
    static final class AdjustItemQuantity implements Command<Confirmation>, CompressedJsonable {
        public final String itemId;
        public final int quantity;
        public final ActorRef<Confirmation> replyTo;

        @JsonCreator
        AdjustItemQuantity(String itemId, int quantity, ActorRef<Confirmation> replyTo) {
            this.itemId = Preconditions.checkNotNull(itemId, "itemId");
            this.quantity = quantity;
            this.replyTo = replyTo;
        }
    }

    static final class Get implements Command<Summary> {
        private final ActorRef<Summary> replyTo;

        @JsonCreator
        Get(ActorRef<Summary> replyTo) {
            this.replyTo = replyTo;
        }
    }

    static final class Checkout implements Command<Confirmation> {
        private final ActorRef<Confirmation> replyTo;

        @JsonCreator
        Checkout(ActorRef<Confirmation> replyTo) {
            this.replyTo = replyTo;
        }
    }

    //
    // SHOPPING CART REPLIES
    //
    interface Reply extends Jsonable {}

    interface Confirmation extends Reply {}

    @Value
    @JsonDeserialize
    static final class Summary implements Reply {

        public final Map<String, Integer> items;
        public final boolean checkedOut;
        public final Optional<Instant> checkoutDate;

        @JsonCreator
        Summary(Map<String, Integer> items, boolean checkedOut, Optional<Instant> checkoutDate) {
            this.items = items;
            this.checkedOut = checkedOut;
            this.checkoutDate = checkoutDate;
        }
    }

    @Value
    @JsonDeserialize
    static final class Accepted implements Confirmation {
        public final Summary summary;

        @JsonCreator
        Accepted(Summary summary) {
            this.summary = summary;
        }
    }

    @Value
    @JsonDeserialize
    static final class Rejected implements Confirmation {
        public final String reason;

        @JsonCreator
        Rejected(String reason) {
            this.reason = reason;
        }
    }

    //
    // SHOPPING CART EVENTS
    //
    public interface Event extends Jsonable, AggregateEvent<Event> {
        /**
         * The tag for shopping cart events, used for consuming the Journal event stream later.
         */
        AggregateEventShards<Event> TAG = AggregateEventTag.sharded(Event.class, 10);

        @Override
        default AggregateEventTagger<Event> aggregateTag() {
            return TAG;
        }
    }

    @Value
    @JsonDeserialize
    static final class ItemAdded implements Event {
        public final String shoppingCartId;
        public final String itemId;
        public final int quantity;
        public final Instant eventTime;

        @JsonCreator
        ItemAdded(String shoppingCartId, String itemId, int quantity, Instant eventTime) {
            this.shoppingCartId = Preconditions.checkNotNull(shoppingCartId, "shoppingCartId");
            this.itemId = Preconditions.checkNotNull(itemId, "itemId");
            this.quantity = quantity;
            this.eventTime = eventTime;
        }
    }

    @Value
    @JsonDeserialize
    static final class ItemRemoved implements Event {
        public final String shoppingCartId;
        public final String itemId;
        public final Instant eventTime;

        @JsonCreator
        ItemRemoved(String shoppingCartId, String itemId, Instant eventTime) {
            this.shoppingCartId = Preconditions.checkNotNull(shoppingCartId, "shoppingCartId");
            this.itemId = Preconditions.checkNotNull(itemId, "itemId");
            this.eventTime = eventTime;
        }
    }

    @Value
    @JsonDeserialize
    static final class ItemQuantityAdjusted implements Event {
        public final String shoppingCartId;
        public final String itemId;
        public final int quantity;
        public final Instant eventTime;

        @JsonCreator
        ItemQuantityAdjusted(String shoppingCartId, String itemId, int quantity, Instant eventTime) {
            this.shoppingCartId = Preconditions.checkNotNull(shoppingCartId, "shoppingCartId");
            this.itemId = Preconditions.checkNotNull(itemId, "itemId");
            this.quantity = quantity;
            this.eventTime = eventTime;
        }
    }

    @Value
    @JsonDeserialize
    static final class CheckedOut implements Event {

        public final String shoppingCartId;
        public final Instant eventTime;

        @JsonCreator
        CheckedOut(String shoppingCartId, Instant eventTime) {
            this.shoppingCartId = Preconditions.checkNotNull(shoppingCartId, "shoppingCartId");
            this.eventTime = eventTime;
        }
    }

    //
    // SHOPPING CART STATE
    //
    @Value
    @JsonDeserialize
    static final class ShoppingCart implements CompressedJsonable {

        public final PMap<String, Integer> items;
        public final Optional<Instant> checkoutDate;

        @JsonCreator
        ShoppingCart(PMap<String, Integer> items, Instant checkoutDate) {
            this.items = Preconditions.checkNotNull(items, "items");
            this.checkoutDate = Optional.ofNullable(checkoutDate);
        }

        ShoppingCart removeItem(String itemId) {
            PMap<String, Integer> newItems = items.minus(itemId);
            return new ShoppingCart(newItems, null);
        }

        ShoppingCart updateItem(String itemId, int quantity) {
            PMap<String, Integer> newItems = items.plus(itemId, quantity);
            return new ShoppingCart(newItems, null);
        }

        boolean isEmpty() {
            return items.isEmpty();
        }

        boolean hasItem(String itemId) {
            return items.containsKey(itemId);
        }

        ShoppingCart checkout(Instant when) {
            return new ShoppingCart(items, when);
        }

        boolean isOpen() {
            return !this.isCheckedOut();
        }

        boolean isCheckedOut() {
            return this.checkoutDate.isPresent();
        }

        public static final ShoppingCart EMPTY = new ShoppingCart(HashTreePMap.empty(), null);
    }

    @Override
    public ShoppingCart emptyState() {
        return ShoppingCart.EMPTY;
    }

    @Override
    public RetentionCriteria retentionCriteria() {
       return RetentionCriteria.snapshotEvery(100, 2);
    }

    @Override
    public Set<String> tagsFor(Event event) {
        return tagger.apply(event);
    }

    @Override
    public CommandHandlerWithReply<Command, Event, ShoppingCart> commandHandler() {
        CommandHandlerWithReplyBuilder<Command, Event, ShoppingCart> builder = newCommandHandlerWithReplyBuilder();
        builder.forState(ShoppingCart::isOpen)
                .onCommand(AddItem.class, this::onAddItem)
                .onCommand(RemoveItem.class, this::onRemoveItem)
                .onCommand(AdjustItemQuantity.class, this::onAdjustItemQuantity)
                .onCommand(Checkout.class, this::onCheckout);

        builder.forState(ShoppingCart::isCheckedOut)
                .onCommand(AddItem.class, cmd -> Effect().reply(cmd.replyTo, new Rejected("Cannot add an item to a checked-out cart")))
                .onCommand(RemoveItem.class, cmd -> Effect().reply(cmd.replyTo, new Rejected("Cannot remove an item to a checked-out cart")))
                .onCommand(AdjustItemQuantity.class, cmd -> Effect().reply(cmd.replyTo, new Rejected("Cannot adjust item quantity in a checked-out cart")))
                .onCommand(Checkout.class, cmd -> Effect().reply(cmd.replyTo, new Rejected("Cannot checkout a checked-out cart")));

        builder.forAnyState().onCommand(Get.class, this::onGet);
        return builder.build();
    }

    private ReplyEffect<Event, ShoppingCart> onAddItem(ShoppingCart shoppingCart, AddItem cmd) {
        if (shoppingCart.hasItem(cmd.getItemId())) {
            return Effect().reply(cmd.replyTo, new Rejected("Item was already added to this shopping cart"));
        } else if (cmd.getQuantity() <= 0) {
            return Effect().reply(cmd.replyTo, new Rejected("Quantity must be greater than zero"));
        } else {
            return Effect()
                    .persist(new ItemAdded(cartId, cmd.getItemId(), cmd.getQuantity(), Instant.now()))
                    .thenReply(cmd.replyTo, s -> new Accepted(toSummary(s)));
        }
    }

    private ReplyEffect<Event, ShoppingCart> onRemoveItem(ShoppingCart shoppingCart, RemoveItem cmd) {
        if (shoppingCart.hasItem(cmd.getItemId())) {
            return Effect()
                    .persist(new ItemRemoved(cartId, cmd.getItemId(), Instant.now()))
                    .thenReply(cmd.replyTo, updatedShoppingCart -> new Accepted(toSummary(updatedShoppingCart)));
        } else {
            // Remove is idempotent, so we can just return the summary here
            return Effect().reply(cmd.replyTo, new Accepted(toSummary(shoppingCart)));
        }
    }

    private ReplyEffect<Event, ShoppingCart> onAdjustItemQuantity(ShoppingCart shoppingCart, AdjustItemQuantity cmd) {
        if (cmd.getQuantity() <= 0) {
            return Effect().reply(cmd.replyTo, new Rejected("Quantity must be greater than zero"));
        } else if (shoppingCart.hasItem(cmd.getItemId())) {
            return Effect()
                    .persist(new ItemQuantityAdjusted(cartId, cmd.getItemId(), cmd.getQuantity(), Instant.now()))
                    .thenReply(cmd.replyTo, s -> new Accepted(toSummary(s)));
        } else {
            return Effect().reply(cmd.replyTo, new Rejected("Item not found in shopping cart"));
        }
    }

    private ReplyEffect<Event, ShoppingCart> onGet(ShoppingCart shoppingCart, Get cmd) {
        return Effect().reply(cmd.replyTo, toSummary(shoppingCart));
    }

    private ReplyEffect<Event, ShoppingCart> onCheckout(ShoppingCart shoppingCart, Checkout cmd) {
        if (shoppingCart.isEmpty()) {
            return Effect().reply(cmd.replyTo, new Rejected("Cannot checkout empty shopping cart"));
        } else {
            return Effect().persist(new CheckedOut(cartId, Instant.now())).thenReply(cmd.replyTo, s -> new Accepted(toSummary(s)));
        }
    }

    @Override
    public EventHandler<ShoppingCart, Event> eventHandler() {
        return newEventHandlerBuilder()
                .forAnyState()
                .onEvent(ItemAdded.class, (shoppingCart, evt) -> shoppingCart.updateItem(evt.getItemId(), evt.getQuantity()))
                .onEvent(ItemRemoved.class, (shoppingCart, evt) -> shoppingCart.removeItem(evt.getItemId()))
                .onEvent(ItemQuantityAdjusted.class, (shoppingCart, evt) -> shoppingCart.updateItem(evt.getItemId(), evt.getQuantity()))
                .onEvent(CheckedOut.class, (shoppingCart, evt) -> shoppingCart.checkout(evt.getEventTime()))
                .build();
    }

    private Summary toSummary(ShoppingCart shoppingCart) {
        return new Summary(shoppingCart.getItems(), shoppingCart.isCheckedOut(), shoppingCart.getCheckoutDate());
    }
}
