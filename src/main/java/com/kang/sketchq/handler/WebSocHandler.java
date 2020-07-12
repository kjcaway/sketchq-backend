package com.kang.sketchq.handler;

import com.kang.sketchq.publisher.DrawingPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class WebSocHandler implements WebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(WebSocHandler.class);

    final private Flux<String> publisher;
    @Autowired
    public DrawingPublisher drawingPublisher;

    public WebSocHandler(DrawingPublisher drawingPublisher) {
        this.publisher = Flux.create(drawingPublisher).log().publish().autoConnect().log();
    }

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        webSocketSession
                .receive()
                .map(webSocketMessage -> webSocketMessage.getPayloadAsText())
                .doOnNext(drawing -> drawingPublisher.push(drawing))
                .doOnError((error) -> log.error(error.getMessage()))
                .doOnComplete(() -> log.info("Complete event. Session disconnect."))
                .subscribe();

        Flux<WebSocketMessage> message = publisher
                .map(drawings -> webSocketSession.textMessage(drawings));
        return webSocketSession.send(message);

    }

}