package com.kang.sketchq.api.user;

import com.kang.sketchq.type.User;
import com.kang.sketchq.util.CommonUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class UserRouter {
    private final UserRedisClient userRedisClient;

    public UserRouter(UserRedisClient userRedisClient) {
        this.userRedisClient = userRedisClient;
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
                                return userRedisClient.findUsers(roomId.get())
                                        .flatMap(s -> ServerResponse.ok().body(BodyInserters.fromValue(s)));
                            }
                        })
                .POST("/join",
                        serverRequest -> {
                            Mono<User> userMono = serverRequest.bodyToMono(User.class);
                            return userMono.flatMap(user -> {
                                if(user.getRoomId() == null) return ServerResponse.badRequest().body(BodyInserters.empty());

                                user.setId(CommonUtil.getRandomString(8));

                                return userRedisClient.createUser(user)
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
