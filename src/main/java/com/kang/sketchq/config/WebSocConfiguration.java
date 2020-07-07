package com.kang.sketchq.config;

import com.kang.sketchq.handler.WebSocHandler;
import com.kang.sketchq.type.Drawing;
import javafx.event.Event;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebSocConfiguration {
    private final WebSocketHandler webSocketHandler;

    public WebSocConfiguration(WebSocHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Bean
    public HandlerMapping webSocketHandlerMapping() {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/drawing", webSocketHandler);
        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setOrder(1);
        handlerMapping.setUrlMap(map);
        return handlerMapping;
    }

//    @Bean
//    public HandlerMapping webSocketMapping(UnicastProcessor<Drawing> eventPublisher, Flux<Drawing> events) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("/drawing", new WebSocHandler(eventPublisher, events));
//        SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
//        simpleUrlHandlerMapping.setUrlMap(map);
//
//        //Without the order things break :-/
//        simpleUrlHandlerMapping.setOrder(1);
//        return simpleUrlHandlerMapping;
//    }


}
