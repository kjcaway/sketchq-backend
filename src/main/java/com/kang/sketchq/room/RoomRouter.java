package com.kang.sketchq.room;

import com.kang.sketchq.handler.WebSocHandler;
import com.kang.sketchq.room.service.RoomService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Optional;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RoomRouter {
    private final RoomService roomService;
    private final WebSocHandler webSocHandler;

    public RoomRouter(RoomService roomService, WebSocHandler webSocHandler) {
        this.roomService = roomService;
        this.webSocHandler = webSocHandler;
    }

    @Bean
    RouterFunction<ServerResponse> routerList() {
        return route()
                .GET("/users",
                        serverRequest -> {
                            Optional<String> roomId = serverRequest.queryParam("roomId");

                            if (!roomId.isPresent()) {
                                return ServerResponse.badRequest().body(BodyInserters.empty());
                            } else {
                                return roomService.getUserList(roomId.get())
                                        .collectList()
                                        .flatMap(s -> ServerResponse.ok().body(BodyInserters.fromValue(s)));
                            }
                        })
                .build();
    }

}
