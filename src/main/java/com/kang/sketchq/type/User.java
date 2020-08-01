package com.kang.sketchq.type;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
public class User implements Serializable {
    private String id;
    private String name;
    private int roomNum;

    public User(String id, String name, int roomNum) {
        this.id = id;
        this.name = name;
        this.roomNum = roomNum;
    }
}
