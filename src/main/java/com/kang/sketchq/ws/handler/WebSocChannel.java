package com.kang.sketchq.ws.handler;

import com.kang.sketchq.ws.publisher.MessagePublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class WebSocChannel{
    private Map<String, MessagePublisher> messagePublisherMap;
    private Map<String, Flux<String>> channelMap;

    @PostConstruct
    private void init() {
        messagePublisherMap = new LinkedHashMap<>();
        channelMap = new LinkedHashMap<>();
    }

    public void addChannel(String roomId) {
        MessagePublisher messagePublisher = new MessagePublisher();
        Flux<String> channel = Flux.create(messagePublisher).share();

        messagePublisherMap.put(roomId, messagePublisher);
        channelMap.put(roomId, channel);
    }

    public MessagePublisher getMessaagePublisher(String roomId) {
        return messagePublisherMap.get(roomId);
    }

    public Flux<String> getChannel(String roomId) {
        return channelMap.get(roomId);
    }

    public void removeChannel(String roomId) {
        messagePublisherMap.remove(roomId);
        channelMap.remove(roomId);
    }
}
