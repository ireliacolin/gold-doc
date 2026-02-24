package com.golddoc.websocket.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.golddoc.websocket.model.WebSocketMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
public class WebSocketClient {
    
    private final String serverUrl;
    private final String userId;
    private EventLoopGroup group;
    private Channel channel;
    private WebSocketClientHandler handler;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Consumer<WebSocketMessage> messageConsumer;
    
    public WebSocketClient(String serverUrl, String userId) {
        this.serverUrl = serverUrl;
        this.userId = userId;
    }
    
    public void connect() throws InterruptedException, URISyntaxException {
        log.info("Connecting to WebSocket server: {} as user: {}", serverUrl, userId);
        
        group = new NioEventLoopGroup();
        
        try {
            URI uri = new URI(serverUrl);
            String host = uri.getHost();
            int port = uri.getPort() == -1 ? 80 : uri.getPort();
            String path = uri.getPath();
            
            // 创建握手器
            WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                    uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders());
            
            // 创建处理器
            handler = new WebSocketClientHandler(handshaker, userId);
            handler.setMessageConsumer(message -> {
                if (messageConsumer != null) {
                    messageConsumer.accept(message);
                }
            });
            
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(
                                    new HttpClientCodec(),
                                    new HttpObjectAggregator(8192),
                                    handler
                            );
                        }
                    });
            
            // 连接服务器
            channel = bootstrap.connect(host, port).sync().channel();
            handler.handshakeFuture().sync();
            
            log.info("WebSocket client connected successfully");
            
            // 发送连接消息
            sendConnectMessage();
            
        } catch (Exception e) {
            log.error("Failed to connect to WebSocket server", e);
            throw e;
        }
    }
    
    private void sendConnectMessage() throws Exception {
        WebSocketMessage connectMessage = WebSocketMessage.connect(userId);
        sendMessage(connectMessage);
    }
    
    public void sendMessage(WebSocketMessage message) throws Exception {
        if (channel == null || !channel.isActive()) {
            throw new IllegalStateException("WebSocket client is not connected");
        }
        
        String jsonMessage = objectMapper.writeValueAsString(message);
        TextWebSocketFrame frame = new TextWebSocketFrame(jsonMessage);
        channel.writeAndFlush(frame);
        log.debug("Sent message: {}", jsonMessage);
    }
    
    public void sendTextMessage(String content, String room) throws Exception {
        WebSocketMessage message = WebSocketMessage.message(userId, content, room);
        sendMessage(message);
    }
    
    public void sendBroadcast(String content) throws Exception {
        WebSocketMessage message = WebSocketMessage.broadcast(userId, content);
        sendMessage(message);
    }
    
    public void disconnect() throws Exception {
        if (channel != null && channel.isActive()) {
            WebSocketMessage disconnectMessage = WebSocketMessage.disconnect(userId);
            sendMessage(disconnectMessage);
            
            // 等待消息发送
            Thread.sleep(100);
        }
        
        close();
    }
    
    public void close() {
        if (channel != null) {
            channel.close();
        }
        
        if (group != null) {
            group.shutdownGracefully();
        }
        
        log.info("WebSocket client disconnected");
    }
    
    public boolean isConnected() {
        return channel != null && channel.isActive();
    }
    
    public void setMessageConsumer(Consumer<WebSocketMessage> messageConsumer) {
        this.messageConsumer = messageConsumer;
    }
    
    public void waitForClose() throws InterruptedException {
        if (channel != null) {
            channel.closeFuture().sync();
        }
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: WebSocketClient <serverUrl> <userId>");
            System.out.println("Example: WebSocketClient ws://localhost:8081/ws user1");
            return;
        }
        
        String serverUrl = args[0];
        String userId = args[1];
        
        WebSocketClient client = new WebSocketClient(serverUrl, userId);
        
        // 设置消息处理器
        client.setMessageConsumer(message -> {
            System.out.printf("[%s] %s: %s%n", 
                    message.getTimestamp(), 
                    message.getSender(),
                    message.getContent());
        });
        
        try {
            client.connect();
            
            // 发送测试消息
            client.sendTextMessage("Hello from " + userId, "general");
            
            // 等待用户输入或自动断开
            System.out.println("Press Enter to disconnect...");
            System.in.read();
            
            client.disconnect();
        } catch (Exception e) {
            log.error("Client error", e);
            client.close();
        }
    }
}