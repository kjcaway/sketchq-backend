package com.kang.sketchq.api.user;

import com.kang.sketchq.type.User;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class UserRedisClient{
    private final ReactiveRedisConnectionFactory factory;
    private final ReactiveRedisOperations<String, Object> reactiveRedisOperations;
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    public UserRedisClient(ReactiveRedisConnectionFactory factory,
                           ReactiveRedisOperations<String, Object> reactiveRedisOperations,
                           ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
        this.factory = factory;
        this.reactiveRedisOperations = reactiveRedisOperations;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    /**
     * Set User
     * @param user
     * @return
     */
    public Mono<Boolean> setUser(User user) {
        return reactiveRedisTemplate.opsForValue().set(user.getRoomId() + ":" + user.getId(), user); // key: "{roomId}:{userId}"
    }

    /**
     * Get User
     * @param roomId
     * @param userId
     * @return
     */
    public Mono<Object> getUser(String roomId, String userId) {
        return reactiveRedisTemplate.opsForValue().get(roomId + ":" + userId);
    }

    /**
     * User List
     * @param roomId
     * @return
     */
    public Mono<List<Object>> scanUsers(String roomId) {
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
