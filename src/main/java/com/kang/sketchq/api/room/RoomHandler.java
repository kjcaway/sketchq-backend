package com.kang.sketchq.api.room;

import com.kang.sketchq.type.Room;
import com.kang.sketchq.ws.handler.WebSocChannel;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class RoomHandler{
    private final RoomRedisClient roomRedisClient;
    private final WebSocChannel webSocChannel;

    public RoomHandler(
            RoomRedisClient roomRedisClient,
            WebSocChannel webSocChannel
    ){
        this.roomRedisClient = roomRedisClient;
        this.webSocChannel = webSocChannel;
    }

    /**
     * Room 최초 생성
     * @param room
     * @return Mono<ServerResponse>
     */
    public Mono<ServerResponse> makeRoom(Room room){
        return roomRedisClient.setRoom(room)
                .flatMap(s -> {
                    webSocChannel.addChannel(room.getId());
                    return ServerResponse.ok().body(BodyInserters.fromValue(room.getId()));
                });
    }
}
