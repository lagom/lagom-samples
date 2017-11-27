package com.lightbend.lagom.recipes.i18n.hello.impl;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.server.PlayServiceCall;
import com.lightbend.lagom.javadsl.server.ServerServiceCall;
import com.lightbend.lagom.recipes.i18n.hello.api.HelloService;
import play.i18n.Lang;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.EssentialAction;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static java.util.Collections.singleton;

/**
 * Implementation of the HelloService.
 */
public class HelloServiceImpl implements HelloService {

    private final MessagesApi messagesApi;

    @Inject
    public HelloServiceImpl(MessagesApi messagesApi) {
        this.messagesApi = messagesApi;
    }

    @Override
    public ServiceCall<NotUsed, String> hello(String id) {
        return localizedServiceCall(messages -> hello(id, messages));
    }

    @Override
    public ServiceCall<NotUsed, String> helloWithLang(Lang lang, String id) {
        return hello(id, messagesApi.preferred(singleton(lang)));
    }

    private ServerServiceCall<NotUsed, String> hello(String id, Messages messages) {
        return request -> {
            String hello = messages.at("hello.message", id);

            return CompletableFuture.completedFuture(hello);
        };
    }

    private <Request, Response> PlayServiceCall<Request, Response> localizedServiceCall(
            Function<Messages, ServerServiceCall<Request, Response>> localizedServiceCall) {

        return wrapCall -> localizedEssentialAction(messages -> wrapCall.apply(localizedServiceCall.apply(messages)));
    }

    private EssentialAction localizedEssentialAction(Function<Messages, EssentialAction> localizedEssentialAction) {
        return EssentialAction.of(requestHeader -> {
            Messages messages = messagesApi.preferred(requestHeader);
            return localizedEssentialAction.apply(messages).apply(requestHeader);
        });
    }

}
