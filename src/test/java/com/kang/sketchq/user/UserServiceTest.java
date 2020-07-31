package com.kang.sketchq.user;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;
    private ReactiveValueOperations<String, String> reactiveValueOps;
    @Before
    public void setup() {
        reactiveValueOps = redisTemplate.opsForValue();
    }

    @Test
    public void givenEmployee_whenSet_thenSet() {
        Mono<Boolean> result = reactiveValueOps.set("tester", "10101");
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    public void givenEmployeeId_whenGet_thenReturnsEmployee() {
        Mono<String> fetchedEmployee = reactiveValueOps.get("tester");
        StepVerifier.create(fetchedEmployee)
                .expectNext("10101")
                .verifyComplete();
    }
}
