package com.example.fileupload.impl;

import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.example.fileupload.api.FileUploadService;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.testkit.ServiceTest.TestServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http.MultipartFormData.DataPart;
import play.mvc.Http.MultipartFormData.FilePart;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.startServer;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static java.util.Arrays.asList;


public class FileUploadServiceTest {

    private static TestServer server;
    private static WSClient ws;

    @BeforeClass
    public static void setUp() {
        server = startServer(defaultSetup().withCluster(false));
        ws = play.test.WSTestClient.newClient(server.port());
    }

    @AfterClass
    public static void tearDown() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    @Test
    public void testUpperCaseEcho() throws ExecutionException, InterruptedException {
        String postPayload = "this is not uppercase";
        ServiceCall<String, String> serviceCall = server.client(FileUploadService.class).uppercaseEcho();
        CompletionStage eventualResponse = serviceCall.invoke(postPayload);
        assertEquals(eventualResponse.toCompletableFuture().get(), "THIS IS NOT UPPERCASE");
    }

    @Test
    public void testFileUploads() throws ExecutionException, InterruptedException, IOException {
        Path originalPath = Paths.get("fileupload-impl", "src", "test", "resources", "sampleFile.txt");
        assertTrue(originalPath.toFile().exists());

        FilePart<Source<ByteString, ?>> filePart = new FilePart<>(
                "multipart-filepart-1",
                "sampleFile.txt",
                "text/plain",
                FileIO.fromPath(originalPath)
        );
        DataPart dataPart = new DataPart("unused-key", "unused-value");
        CompletionStage<WSResponse> eventualResponse = ws.url(String.format("http://localhost:%d/api/files", server.port()))
                .post(Source.from(asList(filePart, dataPart)));

        WSResponse response = eventualResponse.toCompletableFuture().get();
        String[] fileNames = response.getBody().replace("Uploaded[", "").replace("]", "").split(",");
        for (String fileName : fileNames) {
            Path uploadedPath = Paths.get(fileName);
            assertEquals(uploadedPath.toFile().length(), originalPath.toFile().length());
            assertArrayEquals(Files.readAllBytes(uploadedPath), Files.readAllBytes(originalPath));
        }
    }

}
