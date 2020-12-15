package com.kang.sketchq.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kang.sketchq.ws.handler.WebSocChannelService;
import com.kang.sketchq.api.user.service.UserService;
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
    public UserService userService;
    @Autowired
    public WebSocChannelService webSocChannelService;

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

        if(request.getQueryParams().get("roomId") == null || request.getQueryParams().get("userId") == null){
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request"));
        }

        String userId = request.getQueryParams().get("userId").get(0);
        String roomId = request.getQueryParams().get("roomId").get(0);

        exchange.getSession().subscribe((session) -> {
            session.getAttributes().put("userId", userId);
            session.getAttributes().put("roomId", roomId);
        });

        return Mono.just(!userId.isEmpty()).flatMap(b -> {
            if(b){
                log.info("Web socket Connect. request id : " + userId);
                return super.handleRequest(exchange, handler);
            } else{
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request"));
            }
        });
    }
}
