package com.golddoc.websocket;

import com.golddoc.websocket.handler.WebSocketServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
@Slf4j
public class NettyWebSocketServer implements CommandLineRunner {
    
    @Value("${websocket.server.port:8081}")
    private int port;
    
    @Value("${websocket.server.host:0.0.0.0}")
    private String host;
    
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    
    @Override
    public void run(String... args) throws Exception {
        startServer();
    }
    
    public void startServer() throws InterruptedException {
        log.info("Starting Netty WebSocket server on {}:{}", host, port);
        
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new WebSocketServerInitializer())
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true);
            
            ChannelFuture future = bootstrap.bind(host, port).sync();
            serverChannel = future.channel();
            
            log.info("Netty WebSocket server started successfully on {}:{}", host, port);
            log.info("WebSocket endpoint: ws://{}:{}/ws", 
                    host.equals("0.0.0.0") ? "localhost" : host, port);
            
            serverChannel.closeFuture().sync();
        } finally {
            stopServer();
        }
    }
    
    @PreDestroy
    public void stopServer() {
        log.info("Shutting down Netty WebSocket server...");
        
        if (serverChannel != null) {
            serverChannel.close();
        }
        
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        
        log.info("Netty WebSocket server stopped");
    }
    
    public boolean isRunning() {
        return serverChannel != null && serverChannel.isActive();
    }
    
    public int getPort() {
        return port;
    }
    
    public String getHost() {
        return host;
    }
}