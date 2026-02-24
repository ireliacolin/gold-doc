package com.golddoc.websocket.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {
    
    public enum MessageType {
        CONNECT,      // 连接建立
        MESSAGE,      // 普通消息
        BROADCAST,    // 广播消息
        NOTIFICATION, // 通知
        ERROR,        // 错误
        DISCONNECT    // 断开连接
    }
    
    private MessageType type;
    private String sender;
    private String content;
    private LocalDateTime timestamp;
    private String room; // 房间/频道
    
    @JsonCreator
    public WebSocketMessage(
            @JsonProperty("type") MessageType type,
            @JsonProperty("sender") String sender,
            @JsonProperty("content") String content,
            @JsonProperty("room") String room) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.room = room;
        this.timestamp = LocalDateTime.now();
    }
    
    public static WebSocketMessage connect(String sender) {
        return new WebSocketMessage(MessageType.CONNECT, sender, "Connected", null);
    }
    
    public static WebSocketMessage message(String sender, String content, String room) {
        return new WebSocketMessage(MessageType.MESSAGE, sender, content, room);
    }
    
    public static WebSocketMessage broadcast(String sender, String content) {
        return new WebSocketMessage(MessageType.BROADCAST, sender, content, "all");
    }
    
    public static WebSocketMessage notification(String content) {
        return new WebSocketMessage(MessageType.NOTIFICATION, "System", content, null);
    }
    
    public static WebSocketMessage error(String error) {
        return new WebSocketMessage(MessageType.ERROR, "System", error, null);
    }
    
    public static WebSocketMessage disconnect(String sender) {
        return new WebSocketMessage(MessageType.DISCONNECT, sender, "Disconnected", null);
    }
}