package com.example.shoppingcart.impl;

import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.testkit.ReadSideTestDriver;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.bind;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ShoppingCartReportTest {

    private final static ServiceTest.Setup setup = defaultSetup().withJdbc()
            .configureBuilder(b ->
                    b.overrides(
                            bind(ReadSide.class).to(ReadSideTestDriver.class)
                    )
            );

    private static ServiceTest.TestServer testServer;


    private AtomicInteger offset;

    @Before
    public void restartOffset() {
        offset = new AtomicInteger(1);
    }

    @BeforeClass
    public static void beforeAll() {
        testServer = ServiceTest.startServer(setup);
    }

    @AfterClass
    public static void afterAll() {
        if (testServer != null) testServer.stop();
    }

    private ReadSideTestDriver testDriver = testServer.injector().instanceOf(ReadSideTestDriver.class);
    private ReportRepository reportRepository = testServer.injector().instanceOf(ReportRepository.class);

    @Test
    public void createAReportOnFirstEvent() throws InterruptedException, ExecutionException, TimeoutException {

        String cartId = UUID.randomUUID().toString();
        Instant eventTime = Instant.now();
        feed(new ShoppingCartEntity.ItemAdded(cartId, "abc", 2, eventTime));

        ShoppingCartReport report = Await.result(reportRepository.findById(cartId));
        assertEquals("creation date is same as event time", eventTime, report.getCreationDate());
        assertNull("checkout date is not set", report.getCheckoutDate());
    }

    @Test
    public void creationDateDoesNotChangeOnNewEvents() throws InterruptedException, ExecutionException, TimeoutException {
        String cartId = UUID.randomUUID().toString();
        Instant eventTime = Instant.now();
        feed(new ShoppingCartEntity.ItemAdded(cartId, "abc", 1, eventTime));

        ShoppingCartReport report = Await.result(reportRepository.findById(cartId));
        assertEquals("creation date is same as event time", eventTime, report.getCreationDate());
        assertNull("checkout date is not set", report.getCheckoutDate());

        // emit one more event and it should not affect the report
        feed(new ShoppingCartEntity.ItemAdded(cartId, "abc", 2, eventTime.plusSeconds(30)));

        ShoppingCartReport updatedReport = Await.result(reportRepository.findById(cartId));
        assertEquals("creation date is same as first event time", eventTime, updatedReport.getCreationDate());
        assertNull("checkout date is not set", updatedReport.getCheckoutDate());
    }

    @Test
    public void checkoutDateIsSetOnCheckout() throws InterruptedException, ExecutionException, TimeoutException {

        String cartId = UUID.randomUUID().toString();
        Instant eventTime = Instant.now();
        feed(new ShoppingCartEntity.ItemAdded(cartId, "abc", 1, eventTime));

        Instant checkeoutTime = Instant.now().plusSeconds(30);
        feed(new ShoppingCartEntity.CheckedOut(cartId, checkeoutTime));

        ShoppingCartReport report = Await.result(reportRepository.findById(cartId));
        assertEquals("creation date is same as event time", eventTime, report.getCreationDate());
        assertEquals("checkout date is same as checkout date", checkeoutTime, report.getCheckoutDate());
    }


    private void feed(ShoppingCartEntity.Event event) throws InterruptedException, ExecutionException, TimeoutException {
        Await.result(testDriver.feed(event, Offset.sequence(offset.getAndIncrement())));
    }
}
