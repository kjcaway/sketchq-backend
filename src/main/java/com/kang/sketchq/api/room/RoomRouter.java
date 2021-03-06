package com.kang.sketchq.api.room;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kang.sketchq.api.user.UserHandler;
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
    private final RoomRedisClient roomRedisClient;
    private final UserHandler userHandler;
    private final RoomHandler roomHandler;
    final private ObjectMapper jsonMapper = new ObjectMapper();

    public RoomRouter(
            RoomRedisClient roomRedisClient,
            UserHandler userHandler,
            RoomHandler roomHandler) {
        this.roomRedisClient = roomRedisClient;
        this.userHandler = userHandler;
        this.roomHandler = roomHandler;
    }

    @Bean
    RouterFunction<ServerResponse> roomRouterList() {
        return route()
                .POST("/room",
                        serverRequest -> {
                            Mono<Room> roomMono = serverRequest.bodyToMono(Room.class);

                            return roomMono.flatMap(room -> {
                                if(room.getRoomName() == null) return ServerResponse.badRequest().body(BodyInserters.empty());

                                room.setId(CommonUtil.getRandomString(8));
                                room.setCreated(CommonUtil.getNowDateTime("yyyy-MM-dd HH:mm:ss"));

                                return roomHandler.makeRoom(room);
                            });
                    })
                .GET("/rooms",
                        serverRequest -> roomRedisClient.scanRooms()
                                .flatMap(list -> {
                                    if(list.size() > 0){
                                        list.stream()
                                                .sorted((a,b) -> {
                                                    //TODO: 정렬 재 구현
                                                    Room aRoom = jsonMapper.convertValue(a, Room.class);
                                                    Room bRoom = jsonMapper.convertValue(b, Room.class);
                                                    return bRoom.getCreated().compareTo(aRoom.getCreated());
                                                })
                                                .forEach(room -> {
                                                    jsonMapper.convertValue(room, Room.class).setWord(null);
                                                });
                                        return ServerResponse.ok().body(BodyInserters.fromValue(list));
                                    }
                                    return ServerResponse.ok().body(BodyInserters.empty());
                                }))
                .POST("/start",
                        serverRequest -> {
                            Mono<User> userMono = serverRequest.bodyToMono(User.class);

                            return userMono.flatMap(user -> {
                                if(user.getRoomId() == null || user.getId() == null) return ServerResponse.badRequest().body(BodyInserters.empty());

                                Room room = new Room();
                                room.setId(user.getRoomId());
                                room.setWord(CommonUtil.getRandomWord());

                                return roomRedisClient.setWord(room)
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

                                return userHandler.roleChange(user);
                            });
                        })
                .build();
    }
}
