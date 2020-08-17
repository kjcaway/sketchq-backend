package com.kang.sketchq.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kang.sketchq.publisher.WebSocChannelPublisher;
import com.kang.sketchq.type.Message;
import com.kang.sketchq.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Component
public class WebSocHandler implements WebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(WebSocHandler.class);

    final private ObjectMapper jsonMapper = new ObjectMapper();

    @Autowired
    public WebSocChannelPublisher webSocChannelPublisher;

    @Autowired
    public UserService userService;

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        String roomId = webSocketSession.getHandshakeInfo().getAttributes().get("roomId").toString();
        String userId = webSocketSession.getHandshakeInfo().getAttributes().get("userId").toString();

        webSocketSession
                .receive()
                .map(webSocketMessage -> webSocketMessage.getPayloadAsText())
                .map(message -> this.toEvent(message, webSocketSession))
                .doOnNext(message -> webSocChannelPublisher.getMessageQueue(roomId).push(message))
                .doOnError((error) -> log.error(error.getMessage()))
                .doOnComplete(() -> {
                    log.info("Complete event. Session disconnect. User: " + userId);
                    userService.deleteUser(roomId+":"+userId);
                })
                .subscribe();

        return webSocketSession.send(webSocChannelPublisher.getChannel(roomId).map(webSocketSession::textMessage));
    }

    private String toEvent(String message, WebSocketSession webSocketSession) {
        String res = "";
        String userId = webSocketSession.getHandshakeInfo().getAttributes().get("userId").toString();
        try {
            final Message messageObj = jsonMapper.readValue(message, Message.class);
            switch (messageObj.getMessageType()) {
                case JOIN:
                    log.info("Session JOIN: " + userId);
                    break;
                case LEAVE:
                    log.info("Session LEAVE: " + userId);
                    break;
                case CHAT:
                    log.info("User(" + userId + ") CHAT: " + messageObj.getChat());
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