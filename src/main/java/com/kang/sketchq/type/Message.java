package com.kang.sketchq.type;

import lombok.Data;

@Data
public class Message {
    private MessageType messageType;
    private User sender;
    private String chat;
    private Drawing drawing = null;

    public Message() {
        super();
    }

    public Message(MessageType messageType, User sender, String chat, Drawing drawing) {
        this.messageType = messageType;
        this.sender = sender;
        this.chat = chat;
        this.drawing = drawing;
    }

}
