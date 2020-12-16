package com.kang.sketchq.ws.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kang.sketchq.api.room.RoomRedisClient;
import com.kang.sketchq.api.user.UserHandler;
import com.kang.sketchq.type.Message;
import com.kang.sketchq.type.MessageType;
import com.kang.sketchq.type.User;
import com.kang.sketchq.api.user.UserRedisClient;
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
    public WebSocChannel webSocChannel;

    @Autowired
    public UserRedisClient userRedisClient;
    @Autowired
    public RoomRedisClient roomRedisClient;
    @Autowired
    public UserHandler userHandler;

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        String roomId = webSocketSession.getHandshakeInfo().getAttributes().get("roomId").toString();
        String userId = webSocketSession.getHandshakeInfo().getAttributes().get("userId").toString();

        webSocketSession
                .receive()
                .map(webSocketMessage -> webSocketMessage.getPayloadAsText())
                .map(message -> this.toEvent(message, webSocketSession))
                .doOnNext(message -> webSocChannel.getMessaagePublisher(roomId).push(message))
                .doOnError((error) -> log.error(error.getMessage()))
                .doOnComplete(() -> {
                    log.info("doOnComplete. Session disconnect. User: " + userId);

                    userRedisClient.getUser(roomId, userId)
                            .flatMap(user -> {
                                User u =  jsonMapper.convertValue(user, User.class);
                                if(u.getRole() == 1){
                                    userHandler.roleChange(u).subscribe();
                                }
                                return Mono.empty();
                            })
                            .then(userRedisClient.deleteUser(roomId, userId))
                            .then(Mono.just(new Message(MessageType.LEAVE, new User(userId, roomId))))
                            .flatMap(message -> {
                                // Push message
                                try {
                                    String messageStr = jsonMapper.writeValueAsString(message);
                                    webSocChannel.getMessaagePublisher(roomId).push(messageStr);
                                } catch (JsonProcessingException e) {
                                    e.printStackTrace();
                                }
                                return Mono.empty();
                            })
                            .then(userRedisClient.scanUsers(roomId)
                                .flatMap(userList -> {
                                    if(userList.size() == 0){
                                        /* Delete room and channel */
                                        roomRedisClient.deleteRoom(roomId)
                                                .then(roomRedisClient.deleteWord(roomId))
                                                .subscribe();
                                        webSocChannel.removeChannel(roomId);
                                    }
                                    return null;
                                })
                            )
                            .subscribe();
                })
                .subscribe();

        return webSocketSession.send(webSocChannel.getChannel(roomId).map(webSocketSession::textMessage));
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
                    roomRedisClient.getWord(roomId)
                            .flatMap(obj -> {
                                if(messageObj.getChat().equals(obj)){
                                    /* HIT Message push */
                                    log.info("User(" + userId + ") hit word.");
                                    userRedisClient.getUser(roomId, userId)
                                            .subscribe(userObj -> {
                                                User user = jsonMapper.convertValue(userObj, User.class);
                                                Message hitMessage = new Message(MessageType.HIT, user, messageObj.getChat(), null, null);
                                                try {
                                                    String messageStr = jsonMapper.writeValueAsString(hitMessage);
                                                    webSocChannel.getMessaagePublisher(roomId).push(messageStr);

                                                    roomRedisClient.deleteWord("word:"+roomId).subscribe();
                                                } catch (JsonProcessingException e) {
                                                    e.printStackTrace();
                                                }
                                            });
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