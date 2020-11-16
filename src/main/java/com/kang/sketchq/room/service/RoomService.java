package com.kang.sketchq.room.service;

import com.kang.sketchq.publisher.WebSocChannelPublisher;
import com.kang.sketchq.type.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RoomService {
    private final ReactiveRedisConnectionFactory factory;
    private final ReactiveRedisOperations<String, Object> reactiveRedisOperations;
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @Autowired
    public WebSocChannelPublisher webSocChannelPublisher;

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
    public Mono<Long> createRoom(Room room) {
        return reactiveRedisTemplate.opsForList().rightPush("room", room);
    }

    /**
     * Room List
     * @return Length of List
     */
    public Flux<Object> getRoomList() {
        return reactiveRedisTemplate.opsForList().range("room", 0, 100);
    }

    /**
     * Room delete
     * @return Length of List
     */
    public Mono<Long> removeRoom(String room) {
        return reactiveRedisTemplate.opsForList().remove("room", 1, room);
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
     * @param key
     * @return boolean
     */
    public Mono<Object> getWordToRoom(String key) {
        return reactiveRedisTemplate.opsForValue().get(key); // key: "word:{roomId}"
    }

    /**
     * Delete word
     * @param key
     * @return boolean
     */
    public Mono<Long> removeWordToRoom(String key) {
        return reactiveRedisTemplate.delete(key); // key: "word:{roomId}"
    }

//    /**
//     * Room size
//     * @param key
//     * @return Length of List
//     */
//    public Mono<Long> roomSize(String key) {
//        return reactiveRedisTemplate.opsForList().size(key);
//    }
//
//    /**
//     * add User
//     * @param key, id
//     * @return Length of List
//     */
//    public Mono<Long> addUser(String key, String id) {
//        return reactiveRedisTemplate.opsForList().rightPush(key, id);
//    }
//
//    /**
//     * get Users
//     * @param key, id
//     * @return Flux<UserId>
//     */
//    public Flux<Object> getUserList(String key) {
//        return reactiveRedisTemplate.opsForList().range(key, 0, 100);
//    }

}
