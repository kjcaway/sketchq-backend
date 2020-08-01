package com.kang.sketchq.user;

import com.kang.sketchq.type.User;
import com.kang.sketchq.user.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

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
                        serverRequest -> {
                            Optional<String> roomNum = serverRequest.queryParam("roomNum");

                            if(!roomNum.isPresent()){
                                return ServerResponse.badRequest().body(BodyInserters.fromObject("Fail."));
                            } else{
                                return userService.findUsers(Integer.parseInt(roomNum.get()))
                                        .flatMap(s -> ServerResponse.ok().body(BodyInserters.fromObject(s)));
                            }
                        })
                .GET("/join",
                        serverRequest -> {
                            String id = UUID.randomUUID().toString().replace("-", "");
                            Optional<String> name = serverRequest.queryParam("name");
                            Optional<String> roomNum = serverRequest.queryParam("roomNum");
                            User user = new User(id, name.get(), Integer.parseInt(roomNum.get()));

                            return userService.joinUser(user)
                                .flatMap(b -> {
                                    if(b){
                                        return ServerResponse.ok().body(BodyInserters.fromObject("Success."));
                                    } else{
                                        return ServerResponse.badRequest().body(BodyInserters.fromObject("Fail."));
                                    }
                                });

                        })
                .build();
    }

}
