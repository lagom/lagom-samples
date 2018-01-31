/*
 * 
 */
package com.example.hello.impl;

import com.example.hello.api.HelloService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

public class HelloServiceTest {

    public static final String DEFAULT_ETAG = "\"some-value-stored-in-db-or-persistent-entity\"";
    private static TestServer server;

    @BeforeClass
    public static void setUp() {
        server = startServer(defaultSetup().withCluster(false));
    }

    @AfterClass
    public static void tearDown() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    @Test
    public void shouldReturn200AndBodyWhenNoIfNoneMatchIsSent() throws Exception {
        HelloService service = server.client(HelloService.class);
        String msg1 = service.hello("Alice").invoke().toCompletableFuture().get(5, SECONDS);
        assertEquals("Hello Alice", msg1); // response has body
    }

    // Following tests use HttpURLConnection to access the low level properties of the HTTP transport.
    @Test
    public void shouldReturn304AndNoBodyWhen_IfNoneMatch_MatchesETag() throws Exception {
        HttpURLConnection conn = buildReq();
        conn.setRequestProperty("If-None-Match", DEFAULT_ETAG);
        conn.connect();
        assertEquals(304, conn.getResponseCode());
    }

    @Test
    public void shouldReturn200AndABodyWhen_IfNoneMatch_IsInvalid() throws Exception {
        HttpURLConnection conn = buildReq();
        conn.setRequestProperty("If-None-Match", "some-old-etag");
        conn.connect();
        assertEquals(200, conn.getResponseCode());
        assertEquals("Hello alice", readBody(conn));
    }

    @Test
    public void shouldReturnCacheControlAlways() throws Exception {
        // invoking the endpoint without header must return the Cache-Control header
        HttpURLConnection conn1 = buildReq();
        conn1.connect();
        assertEquals("public, max-age=900, s-maxage=1800", conn1.getHeaderField("Cache-Control"));

        // invoking the endpoint with a If-None-Match must return the Cache-Control header too
        HttpURLConnection conn2 = buildReq();
        conn2.setRequestProperty("If-None-Match", DEFAULT_ETAG);
        conn2.connect();
        assertEquals("public, max-age=900, s-maxage=1800", conn2.getHeaderField("Cache-Control"));
    }
    @Test
    public void shouldReturnETagAlways() throws Exception {
        // invoking the endpoint without header must return the ETag header
        HttpURLConnection conn1 = buildReq();
        conn1.connect();
        assertEquals(DEFAULT_ETAG, conn1.getHeaderField("ETag"));

        // invoking the endpoint with a If-None-Match must return the ETag header too
        HttpURLConnection conn2 = buildReq();
        conn2.setRequestProperty("If-None-Match", DEFAULT_ETAG);
        conn2.connect();
        assertEquals(DEFAULT_ETAG, conn2.getHeaderField("ETag"));
    }


    // ------------------------------------------------------------------------

    private String readBody(HttpURLConnection conn) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private HttpURLConnection buildReq() throws IOException {
        return (HttpURLConnection) new URL("http", "localhost", server.port(), "/api/hello/alice")
                .openConnection();
    }
}
