package com.kang.sketchq.api.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kang.sketchq.type.Message;
import com.kang.sketchq.type.MessageType;
import com.kang.sketchq.type.User;
import com.kang.sketchq.ws.handler.WebSocChannel;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Component
public class UserHandler{

    private final UserRedisClient userRedisClient;
    private final WebSocChannel webSocChannel;
    final private ObjectMapper jsonMapper = new ObjectMapper();

    public UserHandler(UserRedisClient userRedisClient, WebSocChannel webSocChannel){
        this.userRedisClient = userRedisClient;
        this.webSocChannel = webSocChannel;
    }

    /**
     * 방장 권한 변경
     * @param user
     * @return Mono<ServerResponse>
     */
    public Mono<ServerResponse> roleChange(User user){
        return userRedisClient.scanUsers(user.getRoomId())
                .flatMap(userList -> {
                    if(userList.size() > 1){
                        // UserList ignored myself
                        List<Object> targetList = userList.stream().filter(t -> {
                            User u =  jsonMapper.convertValue(t, User.class);
                            return !u.getId().equals(user.getId());
                        }).collect(Collectors.toList());

                        // Pick user randomly
                        Random r = new Random();
                        User u = jsonMapper.convertValue(targetList.get(r.nextInt(targetList.size())), User.class);

                        // ROLE message push
                        Message message = new Message(MessageType.ROLECHANGE, u, null, null, null);
                        try {
                            String messageStr = jsonMapper.writeValueAsString(message);
                            webSocChannel.getMessaagePublisher(user.getRoomId()).push(messageStr);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }
                    return ServerResponse.ok().body(BodyInserters.empty());
                });
    }
}
