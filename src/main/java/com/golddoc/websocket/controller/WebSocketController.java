package com.golddoc.websocket.controller;

import com.golddoc.websocket.handler.WebSocketServerHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/websocket")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "WebSocket Management", description = "APIs for managing WebSocket connections")
public class WebSocketController {
    
    @GetMapping("/status")
    @Operation(summary = "Get WebSocket server status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("connectedUsers", WebSocketServerHandler.getConnectedUsersCount());
        status.put("userRooms", WebSocketServerHandler.getUserRooms());
        status.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/health")
    @Operation(summary = "WebSocket health check")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Netty WebSocket Server");
        response.put("connectedUsers", String.valueOf(WebSocketServerHandler.getConnectedUsersCount()));
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/info")
    @Operation(summary = "Get WebSocket connection information")
    public ResponseEntity<Map<String, String>> getConnectionInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("websocketEndpoint", "ws://localhost:8081/ws");
        info.put("connectionExample", "Connect using: new WebSocketClient(\"ws://localhost:8081/ws\", \"your-user-id\")");
        info.put("messageFormat", "JSON: {\"type\":\"MESSAGE\",\"sender\":\"user\",\"content\":\"message\",\"room\":\"general\"}");
        info.put("messageTypes", "CONNECT, MESSAGE, BROADCAST, NOTIFICATION, ERROR, DISCONNECT");
        return ResponseEntity.ok(info);
    }
}