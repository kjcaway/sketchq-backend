package com.kang.sketchq.api.user.service;

import com.kang.sketchq.type.User;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
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

    /**
     * Create User
     * @param user
     * @return
     */
    public Mono<Boolean> createUser(User user) {
        return reactiveRedisTemplate.opsForValue().set(user.getRoomId() + ":" + user.getId(), user); // key: "{roomId}:{userId}"
    }

    /**
     * Find User
     * @param roomId
     * @param userId
     * @return
     */
    public Mono<Object> findUser(String roomId, String userId) {
        return reactiveRedisTemplate.opsForValue().get(roomId + ":" + userId);
    }

    /**
     * Find User List
     * @param roomId
     * @return
     */
    public Mono<List<Object>> findUsers(String roomId) {
        ScanOptions options = ScanOptions.scanOptions().match(roomId + ":*").count(100).build();
        return reactiveRedisOperations
                .scan(options)
                .flatMap(key -> reactiveRedisOperations.opsForValue().get(key))
                .collectList();
    }

    /**
     * Delete User
     * @param roomId
     * @param userId
     * @return
     */
    public Mono<Long> deleteUser(String roomId, String userId) {
        return reactiveRedisTemplate.delete(roomId + ":" + userId);
    }
}
