package com.kang.sketchq.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kang.sketchq.publisher.WebSocChannelPublisher;
import com.kang.sketchq.room.service.RoomService;
import com.kang.sketchq.type.Message;
import com.kang.sketchq.type.MessageType;
import com.kang.sketchq.type.Room;
import com.kang.sketchq.type.User;
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
    @Autowired
    public RoomService roomService;

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
                    log.info("doOnComplete. Session disconnect. User: " + userId);

                    userService.deleteUser(roomId+":"+userId)
                            .flatMap(b -> {
                                /* Leave Message push */
                                User user = new User(userId, roomId);
                                Message message = new Message(MessageType.LEAVE, user, null, null, null);
                                try {
                                    String messageStr = jsonMapper.writeValueAsString(message);
                                    webSocChannelPublisher.getMessageQueue(roomId).push(messageStr);
                                } catch (JsonProcessingException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            })
                            .subscribe();

                    userService.findUsers(roomId)
                            .flatMap(userList -> {
                                if(userList.size() == 0){
                                    /* Delete Room */
                                    roomService.removeRoom("room:" + roomId).subscribe();
                                    roomService.removeWordToRoom("word" + roomId).subscribe();

                                    webSocChannelPublisher.removeChannel(roomId);
                                }
                                return null;
                            }).subscribe();

                })
                .subscribe();

        return webSocketSession.send(webSocChannelPublisher.getChannel(roomId).map(webSocketSession::textMessage));
    }

    private String toEvent(String message, WebSocketSession webSocketSession) {
        String res = "";
        String userId = webSocketSession.getHandshakeInfo().getAttributes().get("userId").toString();
        String roomId = webSocketSession.getHandshakeInfo().getAttributes().get("roomId").toString();
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
                    log.info("User(" + userId + ") chat: " + messageObj.getChat());
                    roomService.getWordToRoom("word:"+roomId)
                            .flatMap(obj -> {
                                if(messageObj.getChat().equals(obj)){
                                    /* HIT Message push */
                                    log.info("User(" + userId + ") hit word.");

                                    User user = new User(userId, roomId);
                                    Message hitMessage = new Message(MessageType.HIT, user, null, null, null);
                                    try {
                                        String messageStr = jsonMapper.writeValueAsString(hitMessage);
                                        webSocChannelPublisher.getMessageQueue(roomId).push(messageStr);

                                        roomService.removeWordToRoom("word:"+roomId).subscribe();
                                    } catch (JsonProcessingException e) {
                                        e.printStackTrace();
                                    }
                                }
                                return null;
                            })
                            .subscribe();
                    break;
                case START:
                    log.info("User(" + userId + ") start game.");
                    break;
                case DRAW:
                case CLEAR:
                case HIT:
                case ROLECHANGE:
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