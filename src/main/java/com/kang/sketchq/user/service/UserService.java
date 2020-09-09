package com.kang.sketchq.user.service;

import com.kang.sketchq.type.User;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class UserService {
    private final ReactiveRedisConnectionFactory factory;
    private final ReactiveRedisOperations<String, Object> reactiveRedisOperations;
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    public UserService(ReactiveRedisConnectionFactory factory,
                       ReactiveRedisOperations<String, Object> reactiveRedisOperations,
                       ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
        this.factory = factory;
        this.reactiveRedisOperations = reactiveRedisOperations;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    public Mono<Boolean> joinUser(User user) {
        return reactiveRedisTemplate.opsForValue().set(user.getRoomId() + ":" + user.getId(), user); // key: "{roomId}:{userId}"
    }

    public Mono<List<Object>> findUsers(String roomId) {
        return reactiveRedisOperations
                .keys(roomId + ":*")
                .flatMap(key -> reactiveRedisOperations.opsForValue().get(key))
                .collectList();
    }

    public Mono<Long> deleteUser(String key) {
        return reactiveRedisTemplate.delete(key);
    }
}
