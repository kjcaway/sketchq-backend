package com.kang.sketchq.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kang.sketchq.handler.WebSocHandler;
import com.kang.sketchq.type.Message;
import com.kang.sketchq.type.MessageType;
import com.kang.sketchq.type.User;
import com.kang.sketchq.user.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class UserRouter {
    private final UserService userService;
    private final WebSocHandler webSocHandler;

    public UserRouter(UserService userService, WebSocHandler webSocHandler) {
        this.userService = userService;
        this.webSocHandler = webSocHandler;
    }

    @Bean
    RouterFunction<ServerResponse> routerList() {
        return route()
                .GET("/users",
                        serverRequest -> {
                            Optional<String> roomNum = serverRequest.queryParam("roomNum");

                            if (!roomNum.isPresent()) {
                                return ServerResponse.badRequest().body(BodyInserters.empty());
                            } else {
                                return userService.findUsers(Integer.parseInt(roomNum.get()))
                                        .flatMap(s -> ServerResponse.ok().body(BodyInserters.fromObject(s)));
                            }
                        })
                .POST("/join",
                        serverRequest -> {
                            String id = UUID.randomUUID().toString().replace("-", "");
                            Mono<User> userMono = serverRequest.bodyToMono(User.class);
                            return userMono.flatMap(user -> {
                                user.setId(id);
                                return userService.joinUser(user)
                                        .flatMap(b -> {
                                            if (b) {
                                                return ServerResponse.ok().body(BodyInserters.fromObject(id));
                                            } else {
                                                return ServerResponse.badRequest().body(BodyInserters.empty());
                                            }
                                        });
                            });
                        })
                .POST("/leave",
                        serverRequest -> {
                            Mono<User> userMono = serverRequest.bodyToMono(User.class);
                            return userMono.flatMap(user -> {
                                return userService.deleteUser(user.getId())
                                        .flatMap(b -> {
                                            if (b) {

                                                return ServerResponse.ok().body(BodyInserters.fromObject(user.getId()));
                                            } else {
                                                return ServerResponse.badRequest().body(BodyInserters.empty());
                                            }
                                        });
                            });
                        })
                .build();
    }

}
