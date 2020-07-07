package com.kang.sketchq;

import com.kang.sketchq.type.Drawing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

@SpringBootApplication
public class SketchqApplication {

    public static void main(String[] args) {
        SpringApplication.run(SketchqApplication.class, args);
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
    public UnicastProcessor<Drawing> eventPublisher(){
        return UnicastProcessor.create();
    }

    @Bean
    public Flux<Drawing> events(UnicastProcessor<Drawing> eventPublisher) {
        return eventPublisher
                .publish()
                .autoConnect();
    }

}
