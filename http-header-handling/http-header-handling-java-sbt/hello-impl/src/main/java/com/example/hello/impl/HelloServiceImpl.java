/*
 * 
 */
package com.example.hello.impl;

import akka.NotUsed;
import akka.japi.Pair;
import com.example.hello.api.HelloService;
import com.lightbend.lagom.javadsl.api.transport.MessageProtocol;
import com.lightbend.lagom.javadsl.api.transport.ResponseHeader;
import com.lightbend.lagom.javadsl.server.HeaderServiceCall;
import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class HelloServiceImpl implements HelloService {

    @Inject
    public HelloServiceImpl() {
    }

    @Override
    public HeaderServiceCall<NotUsed, String> hello(String id) {
        return (reqHeaders, request) -> {
            Optional<String> ifNoneMatch = reqHeaders.getHeader("If-None-Match");

            // The value of ETag is a version or a timestamp that your codebase will have to
            // store and maintain. In a Lagom service it would make sense to make the ETag a field
            // in the persistent entity or the read-side.
            // You could also keep an in-mem variable with the latest ETag but you would have to
            // grant the value is kept up to date across the cluster which is not trivial.
            // Note that the spec requires ETag values to be enclosed in double quotes
            // https://tools.ietf.org/html/rfc7232
            String currentETag = "\"some-value-stored-in-db-or-persistent-entity\"";

            // This builds a default response Header Map which all possible responses will base
            // their headers on.
            HashPMap<String, PSequence<String>> resHeaderMap =
                    HashTreePMap
                            .<String, PSequence<String>>empty()
                            .plus("ETag", TreePVector.singleton(currentETag))
                            .plus("Cache-Control", TreePVector.singleton("public, max-age=900, s-maxage=1800"));

            if (ifNoneMatch.map(x -> x.equals(currentETag)).orElse(false)) {
                // When 'If-None-Match' is sent and the value equals the current 'ETag' build an empty response
                // and return 304
                ResponseHeader resHeaders = new ResponseHeader(304, new MessageProtocol(), resHeaderMap);
                String responseBody = "";
                return CompletableFuture.completedFuture(Pair.create(resHeaders, responseBody));
            } else {
                // When 'If-None-Match' is not sent on the header or the value doesn't match the current 'ETag'
                // build the content of the response (and return a 200)
                String responseBody = "Hello " + id;
                ResponseHeader resHeaders = new ResponseHeader(200, new MessageProtocol(), resHeaderMap);
                return CompletableFuture.completedFuture(Pair.create(resHeaders, responseBody));
            }

        };
    }


}
