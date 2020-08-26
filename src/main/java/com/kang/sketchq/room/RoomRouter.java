package com.kang.sketchq.room;

import com.kang.sketchq.handler.WebSocHandler;
import com.kang.sketchq.publisher.WebSocChannelPublisher;
import com.kang.sketchq.room.service.RoomService;
import com.kang.sketchq.type.Room;
import com.kang.sketchq.util.CommonUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

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
                            String roomId = CommonUtil.getRandomString(8);

                            if (roomId == null) {
                                return ServerResponse.badRequest().body(BodyInserters.empty());
                            } else {
                                Room room = new Room();
                                room.setId(roomId);

                                return roomService.createRoom(room)
                                        .flatMap(s -> {
                                            webSocChannelPublisher.addChannel(roomId);
                                            return ServerResponse.ok().body(BodyInserters.fromValue(roomId));
                                        });
                            }
                        })
                .build();
    }
}
