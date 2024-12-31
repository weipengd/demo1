package com.yxt.websocketsvc.ws;

import com.yxt.websocketsvc.utils.WebsocketProperties;
import com.yxt.websocketsvc.ws.handler.HeartBeatHandler;
import com.yxt.websocketsvc.ws.handler.WebSocketHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class NettyServer {

    private static final String PROTOCOL = "WebSocket";

    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;

    @Value("${netty.port:8333}")
    private int port;

    private void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.group(bossGroup, workGroup).channel(NioServerSocketChannel.class).localAddress(port)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        log.info("received connection {}", ch.localAddress());
                        //webSocket协议本身是基于http协议的，所以这边也要使用http编解码器
                        ch.pipeline().addLast(new HttpServerCodec());
                        //以块的方式来写的处理器`
                        ch.pipeline().addLast(new ChunkedWriteHandler());
                        //http数据在传输过程中是分段的，HttpObjectAggregator可以将多个段聚合
                        ch.pipeline().addLast(new HttpObjectAggregator(8192));
                        //针对客户端，若90s内无读事件则触发心跳处理方法HeartBeatHandler#userEventTriggered
                        ch.pipeline().addLast(new IdleStateHandler(WebsocketProperties.readerIdleTime,
                                WebsocketProperties.writerIdleTime, WebsocketProperties.allIdleTime, TimeUnit.SECONDS));
                        //自定义空闲状态检测(自定义心跳检测handler)
                        ch.pipeline().addLast(new HeartBeatHandler());
                        //数据帧最大长度，合理设置可避免大数据包攻击服务器
                        ch.pipeline().addLast(new WebSocketServerProtocolHandler("/ws", PROTOCOL, true, 65536 * 10));
                        ch.pipeline().addLast(new WebSocketHandler());

                    }
                });
        // 配置完成，开始绑定server，通过调用sync同步方法阻塞直到绑定成功
        ChannelFuture channelFuture = bootstrap.bind().sync();
        log.info("Server started and listen on:{}", channelFuture.channel().localAddress());
        // 对关闭通道进行监听
        channelFuture.channel().closeFuture().sync();
    }


    @PreDestroy
    public void destroy() throws InterruptedException {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().sync();
        }
        if (workGroup != null) {
            workGroup.shutdownGracefully().sync();
        }
    }

    @PostConstruct()
    public void init() {
        new Thread(() -> {
            try {
                start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

    }

}
