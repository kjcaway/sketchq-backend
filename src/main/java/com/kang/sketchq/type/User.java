package com.kang.sketchq.type;

import lombok.Data;

import java.io.Serializable;

@Data
public class User implements Serializable {
    private String id;
    private String name;
    private String roomId;
    private int role;

    public User() {
        super();
    }

    public User(String id, String roomId) {
        this.id = id;
        this.roomId = roomId;
    }
}
