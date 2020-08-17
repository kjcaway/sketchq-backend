package com.kang.sketchq.type;

import lombok.Data;

import java.io.Serializable;

@Data
public class User implements Serializable {
    private String id;
    private String name;
    private String roomId;

    public User() {
        super();
    }

    public User(String id, String name, String roomId) {
        this.id = id;
        this.name = name;
        this.roomId = roomId;
    }
}
