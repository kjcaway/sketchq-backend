package com.kang.sketchq.type;

import lombok.Data;

import java.io.Serializable;

@Data
public class Room implements Serializable {
    private String id;
    private String creator;

    public Room() {
        super();
    }

    public Room(String id, String creator) {
        this.id = id;
        this.creator = creator;
    }
}
