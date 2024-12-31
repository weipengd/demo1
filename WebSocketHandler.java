package com.yxt.websocketsvc.ws.handler;

import com.alibaba.fastjson.JSON;
import com.yxt.websocketsvc.registry.ClientChannelRegistry;
import com.yxt.websocketsvc.registry.ClientInstanceRegistry;
import com.yxt.websocketsvc.registry.OnlineUserRegistry;
import com.yxt.websocketsvc.registry.bind.AttrBind;
import com.yxt.websocketsvc.registry.bind.ChannelAttrBindHelper;
import com.yxt.websocketsvc.registry.keys.ClientChannelKeys;
import com.yxt.websocketsvc.utils.JwtTokenUtil;
import com.yxt.websocketsvc.ws.WebSocketRequest;
import com.yxt.websocketsvc.ws.WebSocketResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @description: websocket请求处理类
 * @author: dongwp
 * @create: 2021/6/22
 **/
@Slf4j
@Component
@ChannelHandler.Sharable
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    /**
     *  客户端建立连接触发
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    /**
     * 客户端与服务端断开触发
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    /**
     * 服务器接受客户端的数据信息
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
        protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        log.info("channelRead0 received message：{}", msg.text());

        if (StringUtils.isEmpty(msg.text())) {
           log.error("channelRead0 text is null.");
            return;
        }

        WebSocketRequest webSocketRequest;
        try {
            webSocketRequest=JSON.parseObject(msg.text(), WebSocketRequest.class);
        } catch (Exception e) {
            log.error("channelRead0 parseObject error.");
            return;
        }

        //心跳续约
        if (WebSocketRequest.Type.HEARTBEAT.getValue().equals(webSocketRequest.getType())) {
            //心跳注册的实例
            ClientInstanceRegistry.INSTANCE.heartbeat(ctx.channel());
            //心跳在线用户
            OnlineUserRegistry.INSTANCE.heartbeat(ctx.channel());

            ctx.channel().writeAndFlush( new TextWebSocketFrame(WebSocketResponse.getHeartbeat()));
            return;
        }

        //认证
        String userId = JwtTokenUtil.verifyToken(webSocketRequest.getJwt());
        if (StringUtils.isEmpty(userId)) {
            ctx.channel() .writeAndFlush(new TextWebSocketFrame(WebSocketResponse.getFail()));
            ctx.channel().close();
            log.debug("authentication_failed jwt:{}",webSocketRequest.getJwt());
            return;
        }

        //build channel参数
        ClientChannelKeys channelKeys = ClientChannelKeys.builder().id(webSocketRequest.getId())
                .instanceId(webSocketRequest.getInstanceId()).project(webSocketRequest.getProject())
                .module(webSocketRequest.getModule()).userId(userId).build();


        //channel绑定属性
        ChannelAttrBindHelper.INSTANCE.bind(AttrBind.AttrKey.clientId, channelKeys.getClientId(), ctx.channel())
                .bind(AttrBind.AttrKey.userId, channelKeys.getUserId(), ctx.channel())
                .bind(AttrBind.AttrKey.instanceId, channelKeys.getInstanceId(), ctx.channel());

        //检查实例上限
        boolean isMaximum = ClientInstanceRegistry.INSTANCE
                .checkInstanceUpperLimit(channelKeys.getInstanceId(), channelKeys.getClientIdExcludeInstanceId());
        if (isMaximum) {
            ctx.channel().writeAndFlush(new TextWebSocketFrame(WebSocketResponse.getInstanceMaxConnFail()));
            ctx.channel().close();
            log.info("the number of instances reaches the upper limit. clientId:{}", channelKeys.getClientId());
            return;
        }
        //注册channel
        batchRegistry(ctx);

        ctx.channel().writeAndFlush(new TextWebSocketFrame(WebSocketResponse.getSuccess()));
        log.info("connection succeeded clientId：{}", channelKeys.getClientId());

    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("handlerAdded call channelId：{}", ctx.channel().id().asLongText());
        //todo add All container
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.info("handlerRemoved call channelId：{}", ctx.channel().id().asLongText());
        batchUNRegistry(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("exceptionCaught：{} {} ", cause.getMessage(), cause.getCause());
        batchUNRegistry(ctx);
    }

    private void batchRegistry(ChannelHandlerContext ctx){
        //注册channel
        ClientChannelRegistry.INSTANCE.registry(ctx.channel());
        //注册实例
        ClientInstanceRegistry.INSTANCE.registry(ctx.channel());
        //注册在线用户
        OnlineUserRegistry.INSTANCE.registry(ctx.channel());

    }
    private void batchUNRegistry(ChannelHandlerContext ctx){
        ClientChannelRegistry.INSTANCE.unRegistry(ctx.channel());
        ClientInstanceRegistry.INSTANCE.unRegistry(ctx.channel());
        OnlineUserRegistry.INSTANCE.unRegistry(ctx.channel());

    }


}
