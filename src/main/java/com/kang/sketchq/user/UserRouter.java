package com.kang.sketchq.user;

import com.kang.sketchq.type.User;
import com.kang.sketchq.user.service.UserService;
import com.kang.sketchq.util.CommonUtil;
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

    public UserRouter(UserService userService) {
        this.userService = userService;
    }

    @Bean
    RouterFunction<ServerResponse> userRouterList() {
        return route()
                .GET("/users",
                        serverRequest -> {
                            Optional<String> roomId = serverRequest.queryParam("roomId");

                            if (!roomId.isPresent()) {
                                return ServerResponse.badRequest().body(BodyInserters.empty());
                            } else {
                                return userService.findUsers(roomId.get())
                                        .flatMap(s -> ServerResponse.ok().body(BodyInserters.fromValue(s)));
                            }
                        })
                .POST("/join",
                        serverRequest -> {
                            String id = CommonUtil.getRandomString(8);
                            Mono<User> userMono = serverRequest.bodyToMono(User.class);
                            return userMono.flatMap(user -> {
                                user.setId(id);

                                if(user.getRoomId() == null) return ServerResponse.badRequest().body(BodyInserters.empty());

                                return userService.joinUser(user)
                                        .flatMap(b -> {
                                            if (b) {
                                                return ServerResponse.ok().body(BodyInserters.fromValue(id));
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

                                                return ServerResponse.ok().body(BodyInserters.fromValue(user.getId()));
                                            } else {
                                                return ServerResponse.badRequest().body(BodyInserters.empty());
                                            }
                                        });
                            });
                        })
                .build();
    }

}
