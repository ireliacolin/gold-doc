package com.golddoc.websocket.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {
    
    private static final String WEBSOCKET_PATH = "/ws";
    
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        
        // 日志处理器
        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
        
        // HTTP编解码器
        pipeline.addLast(new HttpServerCodec());
        
        // 支持大数据流
        pipeline.addLast(new ChunkedWriteHandler());
        
        // 聚合HTTP请求
        pipeline.addLast(new HttpObjectAggregator(65536));
        
        // WebSocket压缩
        pipeline.addLast(new WebSocketServerCompressionHandler());
        
        // WebSocket协议处理器
        pipeline.addLast(new WebSocketServerProtocolHandler(
                WEBSOCKET_PATH,
                null,
                true,
                65536,
                false,
                true,
                10000L
        ));
        
        // 自定义WebSocket处理器
        pipeline.addLast(new WebSocketServerHandler());
    }
}