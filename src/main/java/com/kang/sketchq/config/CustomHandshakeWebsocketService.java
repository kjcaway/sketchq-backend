package com.kang.sketchq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kang.sketchq.publisher.WebSocChannelPublisher;
import com.kang.sketchq.room.Room;
import com.kang.sketchq.room.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.RequestUpgradeStrategy;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class CustomHandshakeWebsocketService extends HandshakeWebSocketService {
    private static final Logger log = LoggerFactory.getLogger(CustomHandshakeWebsocketService.class);
    final private ObjectMapper jsonMapper = new ObjectMapper();

    @Autowired
    public RoomService roomService;
    @Autowired
    public WebSocChannelPublisher webSocChannelPublisher;

    public CustomHandshakeWebsocketService(RequestUpgradeStrategy upgradeStrategy) {
        super(upgradeStrategy);
        this.setSessionAttributePredicate( k -> true);
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

        exchange.getSession().subscribe((session) -> {
            session.getAttributes().put("userId", userId);
            session.getAttributes().put("roomId", roomId);
        });

        return Mono.just(param.isEmpty()).flatMap(s -> {
            if(s){
                // room 생성
                Room room = new Room(roomId, userId);
                webSocChannelPublisher.addChannel(roomId);
                return roomService.createRoom(room);
            } else{
                // room에 user 추가
                return roomService.addUser(roomId, userId);
            }
        }).flatMap(ss -> {
            if(ss != 0){
                log.info("Web socket Connect. request id : " + userId);
                return super.handleRequest(exchange, handler);
            } else{
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request"));
            }
        }).log();
    }
}
