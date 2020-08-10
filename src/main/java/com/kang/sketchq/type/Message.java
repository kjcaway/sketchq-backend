package com.kang.sketchq.type;

import lombok.Data;

@Data
public class Message<T> {
    private MessageType messageType;
    private User sender;
    private String chat;
    private int roomNum;
    private Drawing drawing = null;

    public Message() {
        super();
    }

    public Message(MessageType messageType, User sender, String chat, int roomNum, Drawing drawing) {
        this.messageType = messageType;
        this.sender = sender;
        this.chat = chat;
        this.roomNum = roomNum;
        this.drawing = drawing;
    }

}
