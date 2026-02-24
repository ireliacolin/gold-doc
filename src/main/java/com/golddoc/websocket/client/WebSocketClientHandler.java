package com.golddoc.websocket.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.golddoc.websocket.model.WebSocketMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
    
    private final WebSocketClientHandshaker handshaker;
    private final String userId;
    private ChannelPromise handshakeFuture;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Consumer<WebSocketMessage> messageConsumer;
    
    public WebSocketClientHandler(WebSocketClientHandshaker handshaker, String userId) {
        this.handshaker = handshaker;
        this.userId = userId;
    }
    
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("WebSocket Client disconnected");
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        
        if (!handshaker.isHandshakeComplete()) {
            // 握手未完成，继续握手
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            handshakeFuture.setSuccess();
            log.info("WebSocket Client connected");
            return;
        }
        
        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
                    ", content=" + response.content().toString() + ')');
        }
        
        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            String text = textFrame.text();
            
            try {
                WebSocketMessage message = objectMapper.readValue(text, WebSocketMessage.class);
                log.debug("Received message: {}", text);
                
                if (messageConsumer != null) {
                    messageConsumer.accept(message);
                }
            } catch (Exception e) {
                log.error("Error parsing message: {}", text, e);
            }
            
        } else if (frame instanceof PongWebSocketFrame) {
            log.debug("WebSocket Client received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            log.info("WebSocket Client received closing");
            ch.close();
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("WebSocket Client error", cause);
        
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        
        ctx.close();
    }
    
    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }
    
    public void setMessageConsumer(Consumer<WebSocketMessage> messageConsumer) {
        this.messageConsumer = messageConsumer;
    }
}