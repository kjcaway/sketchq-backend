package com.kang.sketchq.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kang.sketchq.type.Greeting;
import com.kang.sketchq.type.HelloMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GreetingService {
    private static final Logger log = (Logger) LoggerFactory.getLogger(GreetingService.class);
    private ObjectMapper jsonMapper = new ObjectMapper();

    public String greeting(String message){
        String res = null;
        try {
            final HelloMessage helloMessage = jsonMapper.readValue(message, HelloMessage.class);
            final Greeting greeting = Greeting.from(helloMessage);
            res = jsonMapper.writeValueAsString(greeting);
        } catch (Exception e) {
            log.error(e.getMessage());
            res = "";
        } finally {
            return res;
        }
    }
}
