package com.kang.sketchq.handler;

import com.kang.sketchq.publisher.GreetingPublisher;
import com.kang.sketchq.service.GreetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class WebSocHandler implements WebSocketHandler {

    private final GreetingService greetingService = new GreetingService();
    private final GreetingPublisher greetingPublisher;
    private final Flux<String> publisher;

    public WebSocHandler(GreetingPublisher greetingPublisher) {
        this.greetingPublisher = greetingPublisher;
        this.publisher = Flux.create(greetingPublisher).share();
    }

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        webSocketSession
                .receive()
                .map(webSocketMessage -> webSocketMessage.getPayloadAsText())
                .map(helloMessage -> greetingService.greeting(helloMessage))
                .doOnNext(greeting -> greetingPublisher.push(greeting))
                .subscribe();
        final Flux<WebSocketMessage> message = publisher
                .map(greetings -> webSocketSession.textMessage(greetings));
        return webSocketSession.send(message);

    }
}
