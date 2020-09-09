package com.kang.sketchq.room;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kang.sketchq.publisher.WebSocChannelPublisher;
import com.kang.sketchq.room.service.RoomService;
import com.kang.sketchq.type.Message;
import com.kang.sketchq.type.MessageType;
import com.kang.sketchq.type.Room;
import com.kang.sketchq.type.User;
import com.kang.sketchq.user.service.UserService;
import com.kang.sketchq.util.CommonUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RoomRouter {
    private final RoomService roomService;
    private final UserService userService;
    private final WebSocChannelPublisher webSocChannelPublisher;
    final private ObjectMapper jsonMapper = new ObjectMapper();

    public RoomRouter(RoomService roomService, UserService userService, WebSocChannelPublisher webSocChannelPublisher) {
        this.roomService = roomService;
        this.userService = userService;
        this.webSocChannelPublisher = webSocChannelPublisher;
    }

    @Bean
    RouterFunction<ServerResponse> roomRouterList() {
        return route()
                .POST("/room",
                        serverRequest -> {
                            Mono<Room> roomMono = serverRequest.bodyToMono(Room.class);

                            return roomMono.flatMap(room -> {
                                String roomId = CommonUtil.getRandomString(8);

                                if(room.getRoomName() == null) return ServerResponse.badRequest().body(BodyInserters.empty());

                                room.setId(roomId);

                                return roomService.createRoom(room)
                                        .flatMap(s -> {
                                            webSocChannelPublisher.addChannel(roomId);
                                            return ServerResponse.ok().body(BodyInserters.fromValue(roomId));
                                        });
                            });
                    })
                .GET("/rooms",
                        serverRequest -> roomService.getRoomList()
                                .collectList()
                                .flatMap(s -> ServerResponse.ok().body(BodyInserters.fromValue(s))))
                .POST("/start",
                        serverRequest -> {
                            Mono<User> userMono = serverRequest.bodyToMono(User.class);

                            return userMono.flatMap(user -> {
                                if(user.getRoomId() == null || user.getId() == null) return ServerResponse.badRequest().body(BodyInserters.empty());

                                Room room = new Room();
                                room.setId(user.getRoomId());
                                room.setWord(CommonUtil.getRandomWord());

                                return roomService.setWordToRoom(room)
                                        .flatMap(b -> {
                                            if (b) {
                                                return ServerResponse.ok().body(BodyInserters.fromValue(room.getWord()));
                                            } else {
                                                return ServerResponse.badRequest().body(BodyInserters.empty());
                                            }
                                        });
                            });
                        })
                .POST("/rolechange",
                        serverRequest -> {
                            Mono<User> userMono = serverRequest.bodyToMono(User.class);

                            return userMono.flatMap(user -> {
                                if(user.getRoomId() == null) return ServerResponse.badRequest().body(BodyInserters.empty());

                                return userService.findUsers(user.getRoomId())
                                        .flatMap(userList -> {
                                            if(userList.size() > 1){
                                                // UserList ignored myself
                                                List<Object> targetList = userList.stream().filter(t -> {
                                                    User u = (User) t;
                                                    return !u.getId().equals(user.getId());
                                                }).collect(Collectors.toList());

                                                // Pick User randomly
                                                Random r = new Random();
                                                User u = ((User) targetList.get(r.nextInt(targetList.size())));

                                                // ROLE message push
                                                Message message = new Message(MessageType.ROLECHANGE, u, null, null, null);
                                                try {
                                                    String messageStr = jsonMapper.writeValueAsString(message);
                                                    webSocChannelPublisher.getMessageQueue(user.getRoomId()).push(messageStr);
                                                } catch (JsonProcessingException e) {
                                                    e.printStackTrace();
                                                }
                                                return null;
                                            }
                                            return ServerResponse.ok().body(BodyInserters.empty());
                                        });
                            });
                        })
                .build();
    }
}
