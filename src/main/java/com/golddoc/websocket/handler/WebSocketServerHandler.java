package com.golddoc.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.golddoc.websocket.model.WebSocketMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WebSocketServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    
    private static final Map<String, Channel> userChannels = new ConcurrentHashMap<>();
    private static final Map<String, String> userRooms = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof TextWebSocketFrame) {
            String request = ((TextWebSocketFrame) frame).text();
            log.info("Received message: {}", request);
            
            try {
                WebSocketMessage message = objectMapper.readValue(request, WebSocketMessage.class);
                handleMessage(ctx, message);
            } catch (Exception e) {
                log.error("Error processing message: {}", request, e);
                sendError(ctx, "Invalid message format");
            }
        } else {
            String message = "Unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }
    
    private void handleMessage(ChannelHandlerContext ctx, WebSocketMessage message) throws Exception {
        String userId = message.getSender();
        Channel channel = ctx.channel();
        
        switch (message.getType()) {
            case CONNECT:
                handleConnect(userId, channel);
                break;
            case MESSAGE:
                handleMessage(userId, message);
                break;
            case BROADCAST:
                handleBroadcast(message);
                break;
            case DISCONNECT:
                handleDisconnect(userId);
                break;
            default:
                log.warn("Unknown message type: {}", message.getType());
        }
    }
    
    private void handleConnect(String userId, Channel channel) throws Exception {
        userChannels.put(userId, channel);
        log.info("User {} connected. Total users: {}", userId, userChannels.size());
        
        WebSocketMessage response = WebSocketMessage.connect(userId);
        sendMessage(channel, response);
        
        // 通知其他用户
        WebSocketMessage notification = WebSocketMessage.notification(
                String.format("User %s has joined", userId));
        broadcast(notification, userId);
    }
    
    private void handleMessage(String userId, WebSocketMessage message) throws Exception {
        String room = message.getRoom();
        if (room != null) {
            userRooms.put(userId, room);
            log.info("User {} joined room: {}", userId, room);
        }
        
        // 发送消息到指定房间或广播
        if (room != null && !room.equals("all")) {
            sendToRoom(message, room);
        } else {
            broadcast(message, userId);
        }
    }
    
    private void handleBroadcast(WebSocketMessage message) throws Exception {
        broadcast(message, message.getSender());
    }
    
    private void handleDisconnect(String userId) {
        userChannels.remove(userId);
        userRooms.remove(userId);
        log.info("User {} disconnected. Total users: {}", userId, userChannels.size());
        
        // 通知其他用户
        WebSocketMessage notification = WebSocketMessage.notification(
                String.format("User %s has left", userId));
        try {
            broadcast(notification, userId);
        } catch (Exception e) {
            log.error("Error sending disconnect notification", e);
        }
    }
    
    private void sendToRoom(WebSocketMessage message, String room) throws Exception {
        String jsonMessage = objectMapper.writeValueAsString(message);
        TextWebSocketFrame frame = new TextWebSocketFrame(jsonMessage);
        
        int count = 0;
        for (Map.Entry<String, String> entry : userRooms.entrySet()) {
            if (room.equals(entry.getValue())) {
                Channel channel = userChannels.get(entry.getKey());
                if (channel != null && channel.isActive()) {
                    channel.writeAndFlush(frame.copy());
                    count++;
                }
            }
        }
        log.info("Message sent to room {} ({} users)", room, count);
    }
    
    private void broadcast(WebSocketMessage message, String excludeUser) throws Exception {
        String jsonMessage = objectMapper.writeValueAsString(message);
        TextWebSocketFrame frame = new TextWebSocketFrame(jsonMessage);
        
        int count = 0;
        for (Map.Entry<String, Channel> entry : userChannels.entrySet()) {
            if (!entry.getKey().equals(excludeUser)) {
                Channel channel = entry.getValue();
                if (channel != null && channel.isActive()) {
                    channel.writeAndFlush(frame.copy());
                    count++;
                }
            }
        }
        log.info("Broadcast sent to {} users", count);
    }
    
    private void sendMessage(Channel channel, WebSocketMessage message) throws Exception {
        String jsonMessage = objectMapper.writeValueAsString(message);
        channel.writeAndFlush(new TextWebSocketFrame(jsonMessage));
    }
    
    private void sendError(ChannelHandlerContext ctx, String error) {
        try {
            WebSocketMessage errorMessage = WebSocketMessage.error(error);
            String jsonError = objectMapper.writeValueAsString(errorMessage);
            ctx.channel().writeAndFlush(new TextWebSocketFrame(jsonError));
        } catch (Exception e) {
            log.error("Error sending error message", e);
        }
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("New connection from: {}", ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Connection closed: {}", ctx.channel().remoteAddress());
        
        // 清理断开连接的用户
        String disconnectedUser = null;
        for (Map.Entry<String, Channel> entry : userChannels.entrySet()) {
            if (entry.getValue() == ctx.channel()) {
                disconnectedUser = entry.getKey();
                break;
            }
        }
        
        if (disconnectedUser != null) {
            handleDisconnect(disconnectedUser);
        }
        
        super.channelInactive(ctx);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("WebSocket error: {}", cause.getMessage(), cause);
        ctx.close();
    }
    
    public static int getConnectedUsersCount() {
        return userChannels.size();
    }
    
    public static Map<String, String> getUserRooms() {
        return new ConcurrentHashMap<>(userRooms);
    }
}