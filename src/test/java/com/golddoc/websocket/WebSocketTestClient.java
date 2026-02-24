package com.golddoc.websocket;

import com.golddoc.websocket.client.WebSocketClient;
import com.golddoc.websocket.model.WebSocketMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class WebSocketTestClient {
    
    public static void main(String[] args) throws Exception {
        String serverUrl = "ws://localhost:8081/ws";
        String userId = args.length > 0 ? args[0] : "test-user-" + System.currentTimeMillis() % 1000;
        
        log.info("Starting WebSocket test client for user: {}", userId);
        
        WebSocketClient client = new WebSocketClient(serverUrl, userId);
        CountDownLatch latch = new CountDownLatch(1);
        
        // 设置消息处理器
        client.setMessageConsumer(message -> {
            log.info("Received: [{}] {}: {}",
                    message.getType(),
                    message.getSender(),
                    message.getContent());
            
            if (message.getType() == WebSocketMessage.MessageType.DISCONNECT) {
                latch.countDown();
            }
        });
        
        try {
            // 连接服务器
            client.connect();
            log.info("Connected to WebSocket server");
            
            // 发送连接消息
            Thread.sleep(1000);
            
            // 发送测试消息
            client.sendTextMessage("Hello from " + userId, "general");
            log.info("Sent greeting message");
            
            Thread.sleep(1000);
            
            // 发送广播消息
            client.sendBroadcast("This is a broadcast message from " + userId);
            log.info("Sent broadcast message");
            
            // 交互模式
            Scanner scanner = new Scanner(System.in);
            System.out.println("\n=== WebSocket Test Client ===");
            System.out.println("Commands:");
            System.out.println("  message <room> <text> - Send message to room");
            System.out.println("  broadcast <text>      - Send broadcast message");
            System.out.println("  rooms                 - List available rooms");
            System.out.println("  quit                  - Disconnect and exit");
            System.out.println("=============================\n");
            
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
                
                if (input.equalsIgnoreCase("quit")) {
                    break;
                } else if (input.startsWith("message ")) {
                    String[] parts = input.substring(8).split(" ", 2);
                    if (parts.length == 2) {
                        String room = parts[0];
                        String text = parts[1];
                        client.sendTextMessage(text, room);
                        log.info("Sent message to room '{}': {}", room, text);
                    } else {
                        System.out.println("Usage: message <room> <text>");
                    }
                } else if (input.startsWith("broadcast ")) {
                    String text = input.substring(9);
                    client.sendBroadcast(text);
                    log.info("Sent broadcast: {}", text);
                } else if (input.equalsIgnoreCase("rooms")) {
                    System.out.println("Available rooms: general, chat, support, notifications");
                } else if (!input.isEmpty()) {
                    System.out.println("Unknown command. Type 'quit' to exit.");
                }
            }
            
            scanner.close();
            
            // 断开连接
            client.disconnect();
            log.info("Disconnected from WebSocket server");
            
        } catch (Exception e) {
            log.error("Test client error", e);
        } finally {
            client.close();
        }
        
        log.info("Test client finished");
    }
    
    public static void runSimpleTest(String userId) throws Exception {
        String serverUrl = "ws://localhost:8081/ws";
        
        WebSocketClient client = new WebSocketClient(serverUrl, userId);
        
        client.setMessageConsumer(message -> {
            System.out.printf("[TEST] %s: %s%n", message.getSender(), message.getContent());
        });
        
        try {
            client.connect();
            
            // 发送测试消息
            client.sendTextMessage("Test message from " + userId, "test");
            
            // 等待2秒接收消息
            Thread.sleep(2000);
            
            client.disconnect();
        } finally {
            client.close();
        }
    }
}