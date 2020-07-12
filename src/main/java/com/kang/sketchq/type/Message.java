package com.kang.sketchq.type;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Message {
    private MessageType messageType;
    private String sender;
    private String chat;
    private Drawing drawing = null;

    @JsonCreator
    public Message(
            @JsonProperty("messageType") MessageType messageType,
            @JsonProperty("sender") String sender,
            @JsonProperty("chat") String chat,
            @JsonProperty("drawing") Drawing drawing){
        this.messageType = messageType;
        this.sender = sender;
        this.chat = chat;
        this.drawing = drawing;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getSender() {
        return sender;
    }

    public String getChat() {
        return chat;
    }
}
