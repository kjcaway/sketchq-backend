package com.kang.sketchq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kang.sketchq.handler.WebSocHandler;
import com.kang.sketchq.publisher.MessagePublisher;
import com.kang.sketchq.room.Room;
import com.kang.sketchq.room.service.RoomService;
import com.kang.sketchq.type.Message;
import com.kang.sketchq.type.MessageType;
import com.kang.sketchq.type.User;
import com.kang.sketchq.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.RequestUpgradeStrategy;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

public class CustomHandshakeWebsocketService extends HandshakeWebSocketService {
    private static final Logger log = LoggerFactory.getLogger(CustomHandshakeWebsocketService.class);
    final private ObjectMapper jsonMapper = new ObjectMapper();

    @Autowired
    public MessagePublisher messagePublisher;

    @Autowired
    public RoomService roomService;

    public CustomHandshakeWebsocketService(RequestUpgradeStrategy upgradeStrategy) {
        super(upgradeStrategy);
    }

    @Override
    public Mono<Void> handleRequest(
            ServerWebExchange exchange,
            WebSocketHandler handler
    ) {
        ServerHttpRequest request = exchange.getRequest();
        String userId = request.getId();
        String param = "";

        if(request.getQueryParams().get("roomId") != null){
            param = request.getQueryParams().get("roomId").get(0);
        }
        String roomId = "room:"+ (param.isEmpty()?userId:param);

        return Mono.just(param.isEmpty()).flatMap(s -> {
            if(s){
                // room 생성
                Room room = new Room(roomId, userId);
                return roomService.createRoom(room);
            } else{
                // room에 user 추가
                return roomService.addUser(roomId, userId);
            }
        }).flatMap(ss -> {
            if(ss != 0){
                messagePublisher.push(userId);
                log.info("Web socket Connect. request id : " + userId);
                return super.handleRequest(exchange, handler);
            } else{
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request"));
            }
        });
    }
}
