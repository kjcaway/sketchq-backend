package com.kang.sketchq.type;

import lombok.Data;

import java.io.Serializable;

@Data
public class Room implements Serializable {
    private String id;
    private String roomName;
    private String word;

    public Room() {
        super();
    }

    public Room(String id){
        this.id = id;
    }
}
