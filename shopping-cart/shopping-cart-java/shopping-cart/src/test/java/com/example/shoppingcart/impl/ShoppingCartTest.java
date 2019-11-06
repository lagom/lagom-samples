package com.example.shoppingcart.impl;

import akka.actor.testkit.typed.javadsl.LogCapturing;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.cluster.sharding.typed.javadsl.EntityContext;
import akka.persistence.typed.PersistenceId;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.UUID;

public class ShoppingCartTest {

    private static final String inmemConfig =
            "akka.persistence.journal.plugin = \"akka.persistence.journal.inmem\" \n";

    private static final String snapshotConfig =
            "akka.persistence.snapshot-store.plugin = \"akka.persistence.snapshot-store.local\" \n"
                    + "akka.persistence.snapshot-store.local.dir = \"target/snapshot-"
                    + UUID.randomUUID().toString()
                    + "\" \n";

    private static final String config = inmemConfig + snapshotConfig;

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource(config);

    @Rule
    public final LogCapturing logCapturing = new LogCapturing();

    private String randomId() {
        return UUID.randomUUID().toString();
    }

    private ActorRef<ShoppingCart.Command> createTestCart(String cartId) {
        // Unit testing the Aggregate requires an EntityContext but starting
        // a complete Akka Cluster or sharding the actors is not requried.
        // The actorRef to the shard can be null as it won't be used.
        return testKit.spawn(ShoppingCart.create(new EntityContext<>(ShoppingCart.ENTITY_TYPE_KEY, cartId, null)));
    }
    
    @Test
    public void shouldAddAnItem() {
        String cartId = randomId();
        ActorRef<ShoppingCart.Command> shoppingCart = createTestCart(cartId);
        TestProbe<ShoppingCart.Confirmation> probe = testKit.createTestProbe(ShoppingCart.Confirmation.class);

        String itemId = randomId();
        shoppingCart.tell(new ShoppingCart.AddItem(itemId, 10, probe.ref()));

        ShoppingCart.Accepted accepted = (ShoppingCart.Accepted) probe.receiveMessage();
        Assert.assertTrue(accepted.getSummary().getItems().containsKey(itemId));
    }

    @Test
    public void shouldRemoveAnItem() {
        String cartId = randomId();
        ActorRef<ShoppingCart.Command> shoppingCart = createTestCart(cartId);
        TestProbe<ShoppingCart.Confirmation> probe = testKit.createTestProbe(ShoppingCart.Confirmation.class);

        // First add the item
        String itemId = randomId();
        shoppingCart.tell(new ShoppingCart.AddItem(itemId, 10, probe.ref()));
        probe.expectMessageClass(ShoppingCart.Accepted.class);

        // Let's then remove the item
        shoppingCart.tell(new ShoppingCart.RemoveItem(itemId, probe.ref()));

        // And check it is not there anymore
        ShoppingCart.Accepted accepted = (ShoppingCart.Accepted) probe.receiveMessage();
        Assert.assertFalse(accepted.getSummary().getItems().containsKey(itemId));
    }

    @Test
    public void shouldUpdateItemQuantity() {
        String cartId = randomId();
        ActorRef<ShoppingCart.Command> shoppingCart = createTestCart(cartId);
        TestProbe<ShoppingCart.Confirmation> probe = testKit.createTestProbe(ShoppingCart.Confirmation.class);

        // First add the item
        String itemId = randomId();
        shoppingCart.tell(new ShoppingCart.AddItem(itemId, 10, probe.ref()));
        probe.expectMessageClass(ShoppingCart.Accepted.class);

        // Let's then adjust its quantity
        int newQuantity = 10;
        shoppingCart.tell(new ShoppingCart.AdjustItemQuantity(itemId, newQuantity, probe.ref()));

        // And check the item quantity changed
        ShoppingCart.Accepted accepted = (ShoppingCart.Accepted) probe.receiveMessage();
        int itemQuantity = accepted.getSummary().getItems().get(itemId);
        Assert.assertEquals(itemQuantity, newQuantity);
    }

    @Test
    public void shouldAllowCheckingOut() {
        String cartId = randomId();
        ActorRef<ShoppingCart.Command> shoppingCart = createTestCart(cartId);
        TestProbe<ShoppingCart.Confirmation> probe = testKit.createTestProbe(ShoppingCart.Confirmation.class);

        // First add the item
        String itemId = randomId();
        shoppingCart.tell(new ShoppingCart.AddItem(itemId, 10, probe.ref()));
        probe.expectMessageClass(ShoppingCart.Accepted.class);

        // And then checkout the cart
        shoppingCart.tell(new ShoppingCart.Checkout(probe.ref()));

        // Verify the cart is checked out
        ShoppingCart.Accepted accepted = (ShoppingCart.Accepted) probe.receiveMessage();
        Assert.assertTrue(accepted.getSummary().isCheckedOut());
    }

    @Test
    public void shouldAllowGettingShoppingCartSummary() {
        String cartId = randomId();
        ActorRef<ShoppingCart.Command> shoppingCart = createTestCart(cartId);
        TestProbe<ShoppingCart.Confirmation> probe = testKit.createTestProbe(ShoppingCart.Confirmation.class);

        // First add the item
        String itemId = randomId();
        shoppingCart.tell(new ShoppingCart.AddItem(itemId, 10, probe.ref()));
        probe.expectMessageClass(ShoppingCart.Accepted.class);

        // Get summary
        TestProbe<ShoppingCart.Summary> getProbe = testKit.createTestProbe(ShoppingCart.Summary.class);
        shoppingCart.tell(new ShoppingCart.Get(getProbe.ref()));

        // And then check the summary
        ShoppingCart.Summary summary = getProbe.receiveMessage();
        Assert.assertFalse(summary.isCheckedOut());
        Assert.assertTrue(summary.getItems().containsKey(itemId));
    }

    @Test
    public void shouldNotChangeTheCartWhenRemovingItemThatIsNotThere() {
        String cartId = randomId();
        ActorRef<ShoppingCart.Command> shoppingCart = createTestCart(cartId);
        TestProbe<ShoppingCart.Confirmation> probe = testKit.createTestProbe(ShoppingCart.Confirmation.class);

        // First add the item
        String itemId = randomId();
        shoppingCart.tell(new ShoppingCart.AddItem(itemId, 10, probe.ref()));
        probe.expectMessageClass(ShoppingCart.Accepted.class);

        // Trying to remove item that is not there
        String anotherItemId = randomId();
        shoppingCart.tell(new ShoppingCart.RemoveItem(anotherItemId, probe.ref()));

        // And check the cart was not changed
        ShoppingCart.Accepted accepted = (ShoppingCart.Accepted) probe.receiveMessage();
        Assert.assertFalse(accepted.getSummary().isCheckedOut());
        Assert.assertTrue(accepted.getSummary().getItems().containsKey(itemId));
    }

    @Test
    public void shouldFailWhenAddingANegativeNumberOfItems() {
        String cartId = randomId();
        ActorRef<ShoppingCart.Command> shoppingCart = createTestCart(cartId);
        TestProbe<ShoppingCart.Confirmation> probe = testKit.createTestProbe(ShoppingCart.Confirmation.class);

        String itemId = randomId();
        int negativeQuantity = -10;
        shoppingCart.tell(new ShoppingCart.AddItem(itemId, negativeQuantity, probe.ref()));

        probe.expectMessageClass(ShoppingCart.Rejected.class);
    }

    @Test
    public void shouldFailWhenAdjustingItemQuantityToNegativeNumber() {
        String cartId = randomId();
        ActorRef<ShoppingCart.Command> shoppingCart = createTestCart(cartId);
        TestProbe<ShoppingCart.Confirmation> probe = testKit.createTestProbe(ShoppingCart.Confirmation.class);

        // Add an item that will be adjusted
        String itemId = randomId();
        shoppingCart.tell(new ShoppingCart.AddItem(itemId, 10, probe.ref()));
        probe.expectMessageClass(ShoppingCart.Accepted.class);

        // Trying to adjust to a negative quantity
        int negativeQuantity = -10;
        shoppingCart.tell(new ShoppingCart.AdjustItemQuantity(itemId, negativeQuantity, probe.ref()));

        // Command should fail
        probe.expectMessageClass(ShoppingCart.Rejected.class);
    }

    @Test
    public void shouldFailWhenAdjustingItemQuantityForAnItemThatIsNotInTheCart() {
        String cartId = randomId();
        ActorRef<ShoppingCart.Command> shoppingCart = createTestCart(cartId);
        TestProbe<ShoppingCart.Confirmation> probe = testKit.createTestProbe(ShoppingCart.Confirmation.class);

        // First add the item
        String itemId = randomId();
        shoppingCart.tell(new ShoppingCart.AddItem(itemId, 10, probe.ref()));
        probe.expectMessageClass(ShoppingCart.Accepted.class);

        // But let's adjust the quantity for an item that is not there
        String anotherItemId = randomId();
        int newQuantity = 10;
        shoppingCart.tell(new ShoppingCart.AdjustItemQuantity(anotherItemId, newQuantity, probe.ref()));

        // And check that the command failed
        probe.expectMessageClass(ShoppingCart.Rejected.class);
    }

    @Test
    public void shouldFailWhenAddingItemToCheckedOutCart() {
        String cartId = randomId();
        ActorRef<ShoppingCart.Command> shoppingCart = createTestCart(cartId);
        TestProbe<ShoppingCart.Confirmation> probe = testKit.createTestProbe(ShoppingCart.Confirmation.class);

        // First add the item
        String itemId = randomId();
        shoppingCart.tell(new ShoppingCart.AddItem(itemId, 10, probe.ref()));
        probe.expectMessageClass(ShoppingCart.Accepted.class);

        // And then checkout the cart
        shoppingCart.tell(new ShoppingCart.Checkout(probe.ref()));
        probe.expectMessageClass(ShoppingCart.Accepted.class);

        // And try to add new items should fail
        shoppingCart.tell(new ShoppingCart.AddItem(randomId(), 10, probe.ref()));
        probe.expectMessageClass(ShoppingCart.Rejected.class);
    }

    @Test
    public void shouldFailWhenTryingToCheckOutAShoppingCartThatIsCheckedOutAlready() {
        String cartId = randomId();
        ActorRef<ShoppingCart.Command> shoppingCart = createTestCart(cartId);
        TestProbe<ShoppingCart.Confirmation> probe = testKit.createTestProbe(ShoppingCart.Confirmation.class);

        // First add the item
        String itemId = randomId();
        shoppingCart.tell(new ShoppingCart.AddItem(itemId, 10, probe.ref()));
        probe.expectMessageClass(ShoppingCart.Accepted.class);

        // Then check out the cart
        shoppingCart.tell(new ShoppingCart.Checkout(probe.ref()));
        probe.expectMessageClass(ShoppingCart.Accepted.class);

        // And try to check out the cart again should fail
        shoppingCart.tell(new ShoppingCart.Checkout(probe.ref()));
        probe.expectMessageClass(ShoppingCart.Rejected.class);
    }

    @Test
    public void shouldFailWhenTryingToCheckOutAnEmptyCart() {
        String cartId = randomId();
        ActorRef<ShoppingCart.Command> shoppingCart = createTestCart(cartId);
        TestProbe<ShoppingCart.Confirmation> probe = testKit.createTestProbe(ShoppingCart.Confirmation.class);

        // Cart is empty so it should fail to checkout
        shoppingCart.tell(new ShoppingCart.Checkout(probe.ref()));
        probe.expectMessageClass(ShoppingCart.Rejected.class);
    }
}
