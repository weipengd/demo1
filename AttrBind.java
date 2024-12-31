package com.yxt.websocketsvc.registry.bind;

import io.netty.channel.Channel;

/**
 * @author: dongwp
 * @date: 2021/7/9
 **/
public interface AttrBind {

    AttrBind bind(AttrKey key,String attr, Channel channel);

    String getBindAttr(AttrKey key, Channel channel);

    enum AttrKey {
        userId, clientId,instanceId;
    }
}
