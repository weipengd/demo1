package com.yxt.websocketsvc.ws.handler;

import com.yxt.common.util.DateUtil;
import com.yxt.websocketsvc.ws.WebSocketResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @description: 自定义空闲状态检测(自定义心跳检测handler)
 * @author: dongwp
 * @create: 2021/6/22
 **/
@Slf4j
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        log.info("client ping : " + DateUtil.formatDate(new Date()));
        if (evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent)evt;
            if (event.state()== IdleState.READER_IDLE){
                    ctx.channel().writeAndFlush(
                            new TextWebSocketFrame(WebSocketResponse.getStatus(WebSocketResponse.State.TIME_OUT)));
                    ctx.channel().close();
                    log.error("userEventTriggered timeout channel close");
            }
        }else {
            super.userEventTriggered(ctx,evt);
        }
    }

}
