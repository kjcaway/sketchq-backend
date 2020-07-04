package com.kang.sketchq.handler;

import com.kang.sketchq.publisher.DrawingPublisher;
import com.kang.sketchq.service.DrawingService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class WebSocHandler implements WebSocketHandler {

    private final DrawingService drawingService = new DrawingService();
    private final DrawingPublisher drawingPublisher;
    private final Flux<String> publisher;

    public WebSocHandler(DrawingPublisher drawingPublisher) {
        this.drawingPublisher = drawingPublisher;
        this.publisher = Flux.create(drawingPublisher).share();
    }

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        webSocketSession
                .receive()
                .map(webSocketMessage -> webSocketMessage.getPayloadAsText())
                .map(helloMessage -> drawingService.drawing(helloMessage))
                .doOnNext(drawing -> drawingPublisher.push(drawing))
                .subscribe();
        final Flux<WebSocketMessage> message = publisher
                .map(drawings -> webSocketSession.textMessage(drawings));
        return webSocketSession.send(message);

    }
}
