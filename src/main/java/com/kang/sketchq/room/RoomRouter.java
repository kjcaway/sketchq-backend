package com.kang.sketchq.room;

import com.kang.sketchq.handler.WebSocHandler;
import com.kang.sketchq.publisher.WebSocChannelPublisher;
import com.kang.sketchq.room.service.RoomService;
import com.kang.sketchq.type.Room;
import com.kang.sketchq.type.User;
import com.kang.sketchq.util.CommonUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RoomRouter {
    private final RoomService roomService;
    private final WebSocChannelPublisher webSocChannelPublisher;

    public RoomRouter(RoomService roomService, WebSocChannelPublisher webSocChannelPublisher) {
        this.roomService = roomService;
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
                .build();
    }
}
