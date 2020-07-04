package com.kang.sketchq.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kang.sketchq.type.Drawing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DrawingService {
    private static final Logger log = (Logger) LoggerFactory.getLogger(DrawingService.class);
    private ObjectMapper jsonMapper = new ObjectMapper();

    public String drawing(String message){
        String res = null;
        try {
            final Drawing drawing = jsonMapper.readValue(message, Drawing.class);
            res = jsonMapper.writeValueAsString(drawing);
        } catch (Exception e) {
            log.error(e.getMessage());
            res = "";
        } finally {
            return res;
        }
    }
}
