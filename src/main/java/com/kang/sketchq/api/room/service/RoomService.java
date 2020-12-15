package com.kang.sketchq.api.room.service;

import com.kang.sketchq.type.Room;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class RoomService {
    private final ReactiveRedisConnectionFactory factory;
    private final ReactiveRedisOperations<String, Object> reactiveRedisOperations;
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    public RoomService(ReactiveRedisConnectionFactory factory,
                       ReactiveRedisOperations<String, Object> reactiveRedisOperations,
                       ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
        this.factory = factory;
        this.reactiveRedisOperations = reactiveRedisOperations;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    /**
     * Room create
     * @param room
     * @return Length of List
     */
    public Mono<Boolean> createRoom(Room room) {
        return reactiveRedisTemplate.opsForValue().set("room:" + room.getId(), room); // key: "room:{roomId}"
    }

    /**
     * Room List
     * @return Length of List
     */
    public Mono<List<Object>> getRoomList() {
        return reactiveRedisOperations
                .keys( "room:*")
                .flatMap(key -> reactiveRedisOperations.opsForValue().get(key))
                .collectList();
    }

    /**
     * Room delete
     * @param roomId
     * @return Length of List
     */
    public Mono<Long> removeRoom(String roomId) {
        return reactiveRedisTemplate.delete("room:" + roomId); // key : "room:{roomId}"
    }

    /**
     * Set up word for room
     * @param room
     * @return boolean
     */
    public Mono<Boolean> setWordToRoom(Room room) {
        return reactiveRedisTemplate.opsForValue().set("word:" + room.getId(), room.getWord()); // key: "word:{roomId}"
    }

    /**
     * Get word
     * @param roomId
     * @return boolean
     */
    public Mono<Object> getWordToRoom(String roomId) {
        return reactiveRedisTemplate.opsForValue().get("word:" + roomId); // key: "word:{roomId}"
    }

    /**
     * Delete word
     * @param roomId
     * @return boolean
     */
    public Mono<Long> removeWordToRoom(String roomId) {
        return reactiveRedisTemplate.delete("word:" + roomId); // key: "word:{roomId}"
    }
}
