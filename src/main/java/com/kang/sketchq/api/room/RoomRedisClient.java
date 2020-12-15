package com.kang.sketchq.api.room;

import com.kang.sketchq.type.Room;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class RoomRedisClient{
    private final ReactiveRedisConnectionFactory factory;
    private final ReactiveRedisOperations<String, Object> reactiveRedisOperations;
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    public RoomRedisClient(ReactiveRedisConnectionFactory factory,
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
    public Mono<Boolean> setRoom(Room room) {
        return reactiveRedisTemplate.opsForValue().set("room:" + room.getId(), room); // key: "room:{roomId}"
    }

    /**
     * Room List
     * @return Length of List
     */
    public Mono<List<Object>> scanRooms() {
        ScanOptions options = ScanOptions.scanOptions().match("room:*").count(100).build();
        return reactiveRedisOperations
                .scan(options)
                .flatMap(key -> reactiveRedisOperations.opsForValue().get(key))
                .collectList();
    }

    /**
     * Room delete
     * @param roomId
     * @return Length of List
     */
    public Mono<Long> deleteRoom(String roomId) {
        return reactiveRedisTemplate.delete("room:" + roomId); // key : "room:{roomId}"
    }

    /**
     * Set up word for room
     * @param room
     * @return boolean
     */
    public Mono<Boolean> setWord(Room room) {
        return reactiveRedisTemplate.opsForValue().set("word:" + room.getId(), room.getWord()); // key: "word:{roomId}"
    }

    /**
     * Get word
     * @param roomId
     * @return boolean
     */
    public Mono<Object> getWord(String roomId) {
        return reactiveRedisTemplate.opsForValue().get("word:" + roomId); // key: "word:{roomId}"
    }

    /**
     * Delete word
     * @param roomId
     * @return boolean
     */
    public Mono<Long> deleteWord(String roomId) {
        return reactiveRedisTemplate.delete("word:" + roomId); // key: "word:{roomId}"
    }
}
