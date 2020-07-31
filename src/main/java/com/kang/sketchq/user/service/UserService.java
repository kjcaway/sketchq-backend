package com.kang.sketchq.user.service;

import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class UserService {
    private final ReactiveRedisConnectionFactory factory;
    private final ReactiveRedisOperations<String, String> reactiveRedisOperations;
    private final RedisTemplate<String, String> stringStringRedisTemplate;
    private static final AtomicInteger count = new AtomicInteger(0);


    public UserService(ReactiveRedisConnectionFactory factory,
                       ReactiveRedisOperations<String, String> reactiveRedisOperations,
                       RedisTemplate<String, String> stringStringRedisTemplate) {
        this.factory = factory;
        this.reactiveRedisOperations = reactiveRedisOperations;
        this.stringStringRedisTemplate = stringStringRedisTemplate;
    }

    public void joinUser(String userName, String roomId){
        stringStringRedisTemplate.opsForValue().set(userName,roomId);
    }

    public Flux<String> findUsers(String roomId){
        return reactiveRedisOperations
                .keys("*")
                .flatMap(key -> reactiveRedisOperations.opsForValue().get(key));
    }
}
