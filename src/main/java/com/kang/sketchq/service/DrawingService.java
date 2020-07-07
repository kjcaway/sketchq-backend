package com.kang.sketchq.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kang.sketchq.type.Drawing;
import javafx.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DrawingService {
    private static final Logger log = LoggerFactory.getLogger(DrawingService.class);
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public String drawing(Drawing message){
        String res = null;
        try {
//            final Drawing drawing = jsonMapper.readValue(message, Drawing.class);
            res = jsonMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error(e.getMessage());
            res = "";
        } finally {
            return res;
        }
    }
}
