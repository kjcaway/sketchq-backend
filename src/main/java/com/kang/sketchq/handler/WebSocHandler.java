package com.kang.sketchq.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kang.sketchq.publisher.MessagePublisher;
import com.kang.sketchq.type.Message;
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

    final private ObjectMapper jsonMapper = new ObjectMapper();
    final private Flux<String> publisher;

    @Autowired
    public MessagePublisher messagePublisher;

    public WebSocHandler(MessagePublisher messagePublisher) {
        this.publisher = Flux.create(messagePublisher).share();
    }

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        webSocketSession
                .receive()
                .map(webSocketMessage -> webSocketMessage.getPayloadAsText())
                .map(message -> this.toEvent(message, webSocketSession))
                .doOnNext(message -> messagePublisher.push(message))
                .doOnError((error) -> log.error(error.getMessage()))
                .log()
                .doOnComplete(() -> {
                    log.info("Complete event. Session disconnect. User: " + webSocketSession.getId());
                })
                .subscribe();

        Flux<WebSocketMessage> messageFlux = publisher
                .map(message -> webSocketSession.textMessage(message));
        return webSocketSession.send(messageFlux);
    }

    private String toEvent(String message, WebSocketSession webSocketSession) {
        String res = "";
        String id = webSocketSession.getId();
        try {
            final Message messageObj = jsonMapper.readValue(message, Message.class);
            switch (messageObj.getMessageType()) {
                case JOIN:
                    log.info("Session JOIN: " + id);
                    break;
                case LEAVE:
                    log.info("Session LEAVE: " + id);
                    break;
                case CHAT:
                    log.info("User(" + id + ") CHAT: " + messageObj.getChat());
                    break;
                case DRAW:
                    break;
                default:
                    break;
            }

            res = jsonMapper.writeValueAsString(messageObj);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            return res;
        }
    }
}