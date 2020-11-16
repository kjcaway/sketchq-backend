package com.kang.sketchq.publisher;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class WebSocChannelPublisher {
    private Map<String, MessageQueue> messageQueueMap;
    private Map<String, Flux<String>> channelMap;

    @PostConstruct
    private void init() {
        messageQueueMap = new LinkedHashMap<>();
        channelMap = new LinkedHashMap<>();
    }

    public void addChannel(String roomId) {
        MessageQueue messageQueue = new MessageQueue();
        Flux<String> channel = Flux.create(messageQueue).share();

        messageQueueMap.put(roomId, messageQueue);
        channelMap.put(roomId, channel);
    }

    public MessageQueue getMessageQueue(String roomId) {
        return messageQueueMap.get(roomId);
    }

    public Flux<String> getChannel(String roomId) {
        return channelMap.get(roomId);
    }

    public boolean isEmpty(String roomId) {
        return messageQueueMap.containsKey(roomId);
    }

    public void removeChannel(String roomId) {
        messageQueueMap.remove(roomId);
        channelMap.remove(roomId);
    }
}
