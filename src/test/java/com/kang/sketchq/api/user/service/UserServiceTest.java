package com.kang.sketchq.api.user.service;

//@SpringBootTest
//public class UserServiceTest {
//
//    @Autowired
//    private ReactiveRedisTemplate<String, String> redisTemplate;
//    private ReactiveValueOperations<String, String> reactiveValueOps;
//
//    @Before
//    public void setup() {
//        reactiveValueOps = redisTemplate.opsForValue();
//    }
//
//    @Test
//    public void joinUserTest() {
//        Mono<Boolean> result = reactiveValueOps.set("tester", "10101");
//        StepVerifier.create(result)
//                .expectNext(true)
//                .verifyComplete();
//    }
//
//    @Test
//    public void findUserTest() {
//        Mono<String> fetchedEmployee = reactiveValueOps.get("tester");
//        StepVerifier.create(fetchedEmployee)
//                .expectNext("10101")
//                .verifyComplete();
//    }
//}
