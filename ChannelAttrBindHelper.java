package com.yxt.websocketsvc.registry.bind;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

/**
 * @author: dongwp
 * @date: 2021/7/9
 **/
public class ChannelAttrBindHelper implements AttrBind {

   public static final ChannelAttrBindHelper INSTANCE;

   static  {
        INSTANCE = new ChannelAttrBindHelper();
    }

    @Override
    public ChannelAttrBindHelper bind(AttrKey key, String attr, Channel channel) {
        AttributeKey<String> attributeKey = AttributeKey.valueOf(key.name());
        channel.attr(attributeKey).setIfAbsent(attr);
        return this;
    }

    @Override
    public String getBindAttr(AttrKey key, Channel channel) {
        AttributeKey<String> attributeKey = AttributeKey.valueOf(key.name());
        return channel.attr(attributeKey).get();
    }

    public String getBindAttrClientId(Channel channel){
        AttributeKey<String> attributeKey = AttributeKey.valueOf(AttrKey.clientId.name());
        return channel.attr(attributeKey).get();
    }

    public String getBindAttrUserId(Channel channel){
        AttributeKey<String> attributeKey = AttributeKey.valueOf(AttrKey.userId.name());
        return channel.attr(attributeKey).get();
    }
    public String getBindAttrInstanceId(Channel channel){
        AttributeKey<String> attributeKey = AttributeKey.valueOf(AttrKey.instanceId.name());
        return channel.attr(attributeKey).get();
    }
}
