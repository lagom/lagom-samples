package com.example.shoppingcart.impl;

import akka.Done;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.example.shoppingcart.impl.ShoppingCartCommand.Checkout;
import com.example.shoppingcart.impl.ShoppingCartCommand.Get;
import com.example.shoppingcart.impl.ShoppingCartCommand.UpdateItem;
import com.example.shoppingcart.impl.ShoppingCartEvent.CheckedOut;
import com.example.shoppingcart.impl.ShoppingCartEvent.ItemUpdated;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver.Outcome;
import org.junit.*;

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ShoppingCartEntityTest {
    private static ActorSystem system;
    private static final String ENTITY_ID = "shopping-cart-1";

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("ShoppingCartEntityTest");
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    private PersistentEntityTestDriver<ShoppingCartCommand, ShoppingCartEvent, ShoppingCartState> driver;

    @Before
    public void setupDriver() {
        driver = new PersistentEntityTestDriver<>(system, new ShoppingCartEntity(), ENTITY_ID);
    }

    @After
    public void verifyNoIssues() {
        assertThat(driver.getAllIssues(), empty());
    }

    @Test
    public void shoppingCartShouldAllowAddingAnItem() {
        Outcome<ShoppingCartEvent, ShoppingCartState> outcome = driver.run(new UpdateItem("123", 2));

        assertThat(outcome.getReplies(), contains(Done.getInstance()));

        assertEquals(outcome.events().size(), 1);
        ItemUpdated itemUpdated = (ItemUpdated) outcome.events().get(0);
        assertEquals(itemUpdated.shoppingCartId, ENTITY_ID);
        assertEquals(itemUpdated.productId, "123");
        assertEquals(itemUpdated.quantity, 2);

        assertThat(outcome.state(), equalTo(ShoppingCartState.EMPTY.updateItem("123", 2)));
    }

    @Test
    public void shoppingCartShouldAllowRemovingAnItem() {
        driver.run(new UpdateItem("123", 2));
        Outcome<ShoppingCartEvent, ShoppingCartState> outcome = driver.run(new UpdateItem("123", 0));

        assertThat(outcome.getReplies(), contains(Done.getInstance()));

        assertEquals(outcome.events().size(), 1);
        ItemUpdated itemUpdated = (ItemUpdated) outcome.events().get(0);
        assertEquals(itemUpdated.shoppingCartId, ENTITY_ID);
        assertEquals(itemUpdated.productId, "123");
        assertEquals(itemUpdated.quantity, 0);

        assertThat(outcome.state(), equalTo(ShoppingCartState.EMPTY));
    }

    @Test
    public void shoppingCartShouldAllowUpdatingMultipleItems() {
        assertThat(driver.run(new UpdateItem("123", 2)).state(),
            equalTo(ShoppingCartState.EMPTY.updateItem("123", 2)));
        assertThat(driver.run(new UpdateItem("456", 3)).state(),
            equalTo(ShoppingCartState.EMPTY.updateItem("123", 2).updateItem("456", 3)));
        assertThat(driver.run(new UpdateItem("123", 1)).state(),
            equalTo(ShoppingCartState.EMPTY.updateItem("123", 1).updateItem("456", 3)));
        assertThat(driver.run(new UpdateItem("456", 0)).state(),
            equalTo(ShoppingCartState.EMPTY.updateItem("123", 1)));
    }

    @Test
    public void shoppingCartShouldAllowCheckingOut() {
        driver.run(new UpdateItem("123", 2));
        Outcome<ShoppingCartEvent, ShoppingCartState> outcome = driver.run(Checkout.INSTANCE);

        assertThat(outcome.getReplies(), contains(Done.getInstance()));

        assertEquals(outcome.events().size(), 1);
        CheckedOut checkedOut = (CheckedOut) outcome.events().get(0);
        assertEquals(checkedOut.shoppingCartId, ENTITY_ID);

        assertThat(outcome.state(), equalTo(ShoppingCartState.EMPTY.updateItem("123", 2).checkout()));
    }

    @Test
    public void shoppingCartShouldAllowGettingTheState() {
        driver.run(new UpdateItem("123", 2));
        Outcome<ShoppingCartEvent, ShoppingCartState> outcome = driver.run(Get.INSTANCE);

        assertThat(outcome.getReplies(), contains(ShoppingCartState.EMPTY.updateItem("123", 2)));
        assertThat(outcome.events(), empty());
    }

    @Test
    public void shoppingCartShouldFailWhenRemovingAnItemThatIsntAdded() {
        Outcome<ShoppingCartEvent, ShoppingCartState> outcome = driver.run(new UpdateItem("123", 0));

        assertThat(outcome.getReplies(), contains(instanceOf(ShoppingCartException.class)));
        assertThat(outcome.events(), empty());
    }

    @Test
    public void shoppingCartShouldFailWhenAddingNegativeItems() {
        Outcome<ShoppingCartEvent, ShoppingCartState> outcome = driver.run(new UpdateItem("123", -1));

        assertThat(outcome.getReplies(), contains(instanceOf(ShoppingCartException.class)));
        assertThat(outcome.events(), empty());
    }

    @Test
    public void shoppingCartShouldFailWhenRemovingAnItemToACheckedOutCart() {
        driver.run(new UpdateItem("123", 2), Checkout.INSTANCE);
        Outcome<ShoppingCartEvent, ShoppingCartState> outcome = driver.run(new UpdateItem("456", 2));

        assertThat(outcome.getReplies(), contains(instanceOf(ShoppingCartException.class)));
        assertThat(outcome.events(), empty());
    }

    @Test
    public void shoppingCartShouldFailWhenCheckingOutTwice() {
        driver.run(new UpdateItem("123", 2), Checkout.INSTANCE);
        Outcome<ShoppingCartEvent, ShoppingCartState> outcome = driver.run(Checkout.INSTANCE);

        assertThat(outcome.getReplies(), contains(instanceOf(ShoppingCartException.class)));
        assertThat(outcome.events(), empty());
    }

    @Test
    public void shoppingCartShouldFailWhenCheckingOutEmptyCart() {
        Outcome<ShoppingCartEvent, ShoppingCartState> outcome = driver.run(Checkout.INSTANCE);

        assertThat(outcome.getReplies(), contains(instanceOf(ShoppingCartException.class)));
        assertThat(outcome.events(), empty());
    }
}
