package com.kang.sketchq.type;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Drawing {
    private final String color;
    private final int[] originP;
    private final int[] newP;

    @JsonCreator
    public Drawing(
            @JsonProperty("color") String color,
            @JsonProperty("originP") int[] originP,
            @JsonProperty("newP") int[] newP){
        this.color = color;
        this.originP = originP;
        this.newP = newP;
    }
}
