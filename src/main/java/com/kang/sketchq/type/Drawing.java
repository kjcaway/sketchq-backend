package com.kang.sketchq.type;

import lombok.Data;

import java.io.Serializable;

@Data
public class Drawing implements Serializable {
    private String color;
    private int[] originP;
    private int[] newP;

    public Drawing() {
        super();
    }
}
