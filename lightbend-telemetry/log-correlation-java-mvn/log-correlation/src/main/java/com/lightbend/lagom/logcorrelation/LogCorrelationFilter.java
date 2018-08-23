package com.lightbend.lagom.logcorrelation;

import akka.stream.Materializer;
import com.typesafe.config.Config;
import org.slf4j.MDC;
import play.mvc.Filter;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class LogCorrelationFilter extends Filter {

    private final String correlationId;

    @Inject
    public LogCorrelationFilter(Materializer mat, Config config) {
        super(mat);
        correlationId = config.getConfig("log-correlation").getString("correlation-id");
    }

    @Override
    public CompletionStage<Result> apply(
            Function<Http.RequestHeader, CompletionStage<Result>> nextFilter,
            Http.RequestHeader requestHeader) {

        if (MDC.get(correlationId) == null) {
            MDC.put(correlationId, UUID.randomUUID().toString());
        }

        return nextFilter.apply(requestHeader);
    }
}