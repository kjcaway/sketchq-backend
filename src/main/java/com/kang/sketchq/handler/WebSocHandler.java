package com.kang.sketchq.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kang.sketchq.publisher.DrawingPublisher;
import com.kang.sketchq.type.Drawing;
import com.kang.sketchq.type.Message;
import com.kang.sketchq.type.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.*;

@Component
public class WebSocHandler implements WebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(WebSocHandler.class);

    final private ObjectMapper jsonMapper = new ObjectMapper();
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
                .map(message -> this.toEvent(message, webSocketSession))
                .doOnNext(drawing -> drawingPublisher.push(drawing))
                .doOnError((error) -> log.error(error.getMessage()))
                .doOnComplete(() -> {
                    String username = (String) webSocketSession.getAttributes().get("username");
                    log.info("Complete event. Session disconnect. User: " + username);

                })
                .subscribe();

        Flux<WebSocketMessage> message = publisher
                .map(drawings -> webSocketSession.textMessage(drawings));
        return webSocketSession.send(message);

    }

    private String toEvent(String message, WebSocketSession webSocketSession){
        String res = "";
        try{
            final Message jsonMes = jsonMapper.readValue(message, Message.class);
            switch (jsonMes.getMessageType()){
                case JOIN:
                    log.info("Session connect. User: " + jsonMes.getSender());
                    webSocketSession.getAttributes().putIfAbsent("username", jsonMes.getSender());
                    break;
                default:
                    break;
            }

            res = jsonMapper.writeValueAsString(jsonMes);
        }catch(Exception e){
            log.error(e.getMessage());

        } finally {
            return res;
        }
    }
}