package com.kang.sketchq.type;

import lombok.Data;

@Data
public class Message<T> {
    private MessageType messageType;
    private User sender;
    private String chat;
    private String roomId;
    private Drawing drawing = null;

    public Message() {
        super();
    }

    public Message(MessageType messageType, User sender, String chat, String roomId, Drawing drawing) {
        this.messageType = messageType;
        this.sender = sender;
        this.chat = chat;
        this.roomId = roomId;
        this.drawing = drawing;
    }

}
