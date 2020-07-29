package com.kang.sketchq.user;

import com.kang.sketchq.user.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Optional;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class UserRouter {
    private final UserService userService;

    public UserRouter(UserService userService){
        this.userService = userService;
    }

    @Bean
    RouterFunction<ServerResponse> routerList(){
        return route()
                .GET("/users",
                        serverRequest -> ServerResponse.ok().contentType(MediaType.TEXT_EVENT_STREAM).body(userService.findUsers("room1"), String.class))
                .GET("/join/{name}",
                        serverRequest -> {
                            Optional<String> name = serverRequest.queryParam("name");
                            name.ifPresent(nm -> {
                                userService.joinUser(nm, "room1");
                            });
                            return ServerResponse.ok().body(BodyInserters.fromObject("Success."));
                        })
                .build();
    }

}
