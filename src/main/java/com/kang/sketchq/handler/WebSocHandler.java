package com.kang.sketchq.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kang.sketchq.publisher.DrawingPublisher;
import com.kang.sketchq.service.DrawingService;
import com.kang.sketchq.type.Drawing;
import javafx.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;

import java.io.IOException;

@Component
public class WebSocHandler implements WebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(WebSocHandler.class);



    private final UnicastProcessor<Drawing> eventPublisher;
    private final Flux<String> outputEvents;
    private final ObjectMapper mapper;
    public WebSocHandler(UnicastProcessor<Drawing> eventPublisher, Flux<Drawing> events) {
        this.eventPublisher = eventPublisher;
        this.mapper = new ObjectMapper();
        this.outputEvents = Flux.from(events).map(this::toJSON);
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        WebSocMessageSubscriber subscriber = new WebSocMessageSubscriber(eventPublisher);
        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .map(json -> {
                    try {
                        return mapper.readValue(json, Drawing.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .doOnNext(subscriber::onNext)
                .doOnError(subscriber::onError)
                .doOnComplete(subscriber::onComplete)
                .zipWith(session.send(outputEvents.map(session::textMessage)))
                .then();
    }
    private String toJSON(Drawing event) {
        try {
            return mapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

//    @Autowired
//    public DrawingService drawingService;
//    @Autowired
//    public DrawingPublisher drawingPublisher;
//    private Flux<String> publisher;
//
//    public WebSocHandler(DrawingPublisher drawingPublisher) {
//        this.publisher = Flux.create(drawingPublisher).share();
//    }
//
//        @Override
//    public Mono<Void> handle(WebSocketSession webSocketSession) {
//        webSocketSession
//                .receive()
//                .map(webSocketMessage -> webSocketMessage.getPayloadAsText())
//                .map(helloMessage -> drawingService.drawing(helloMessage))
//                .doOnNext(drawing ->drawingPublisher.push(drawing))
//                .doOnError((error) -> log.error("Error. subscriber..." + error))
//                .doOnComplete(() -> {
//                    log.error("Complete.");
//                })
//                .subscribe();
//
//        Flux<WebSocketMessage> message = publisher
//                .map(drawings -> {
//                    log.debug("webSocketSession.textMessage="+drawings);
//                    return webSocketSession.textMessage(drawings);
//                });
//        return webSocketSession.send(message);
//
//    }

}