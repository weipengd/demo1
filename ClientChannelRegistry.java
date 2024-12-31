package com.yxt.websocketsvc.registry;

import com.yxt.websocketsvc.registry.bind.ChannelAttrBindHelper;
import com.yxt.websocketsvc.registry.keys.ClientChannelKeys;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: dongwp
 * @date: 2021/6/22
 **/
@Component
@Slf4j
public class ClientChannelRegistry implements Registry {

    public static final ClientChannelRegistry INSTANCE;
    static {
        INSTANCE = new ClientChannelRegistry();
    }

    private ConcurrentHashMap<String, Channel> clientChannelMap = new ConcurrentHashMap<>();

    public ConcurrentHashMap<String, Channel> getClientChannelMap() {
        return clientChannelMap;
    }

    @Override
    public void registry(Channel channel) {
        String clientId = ChannelAttrBindHelper.INSTANCE.getBindAttrClientId(channel);
        clientChannelMap.put(clientId, channel);
    }

    @Override
    public void unRegistry(Channel channel) {
        String clientId = ChannelAttrBindHelper.INSTANCE.getBindAttrClientId(channel);
        clientChannelMap.remove(clientId, channel);
    }

    @Override
    public void heartbeat(Channel channel) {
            //do something
    }

    public static ChannelGroup getChannelsWithKeys(ClientChannelKeys request) {
        ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        ConcurrentHashMap<String, Channel> clientChannelMap = ClientChannelRegistry.INSTANCE.getClientChannelMap();
        clientChannelMap.forEach((key, value) -> {
            AtomicInteger atomicInteger = new AtomicInteger(0);
            HashSet<Channel> set = new HashSet();
            if (StringUtils.isNotEmpty(request.getProject())) {
                ClientChannelKeys channelKeys = ClientChannelKeys.toClientChannelKeys(key);
                if (request.getProject().equals(channelKeys.getProject())) {
                    if (atomicInteger.get() <= 0) {
                        set.add(value);
                    }
                } else {
                    set.remove(value);
                    atomicInteger.addAndGet(1);
                }
            }
            if (StringUtils.isNotEmpty(request.getModule())) {
                ClientChannelKeys channelKeys = ClientChannelKeys.toClientChannelKeys(key);
                if (request.getModule().equals(channelKeys.getModule())) {
                    if (atomicInteger.get() <= 0) {
                        set.add(value);
                    }
                } else {
                    set.remove(value);
                    atomicInteger.addAndGet(1);
                }
            }
            if (StringUtils.isNotEmpty(request.getId())) {
                ClientChannelKeys channelKeys = ClientChannelKeys.toClientChannelKeys(key);
                if (request.getId().equals(channelKeys.getId())) {
                    if (atomicInteger.get() <= 0) {
                        set.add(value);
                    }
                } else {
                    set.remove(value);
                    atomicInteger.addAndGet(1);
                }
            }
            if (StringUtils.isNotEmpty(request.getUserId())) {
                ClientChannelKeys channelKeys = ClientChannelKeys.toClientChannelKeys(key);
                if (request.getUserId().equals(channelKeys.getUserId())) {
                    if (atomicInteger.get() <= 0) {
                        set.add(value);
                    }
                } else {
                    set.remove(value);
                    atomicInteger.addAndGet(1);
                }
            }
            if (set.size() > 0) {
                Channel channel = (Channel) Arrays.stream(set.toArray()).findFirst().get();
                channelGroup.add(channel);
            }
        });

        return channelGroup;
    }

    public static List<String> getClientIdsWithKeys(ClientChannelKeys request) {
        List<String> clientIds = new ArrayList<>();
        ClientChannelRegistry.INSTANCE.getClientChannelMap().forEach((key, value) -> {
            AtomicInteger countNum = new AtomicInteger(0);
            if (StringUtils.isNotEmpty(request.getProject())) {
                ClientChannelKeys channelKeys = ClientChannelKeys.toClientChannelKeys(key);
                if (request.getProject().equals(channelKeys.getProject())) {
                    if (countNum.get() <= 0) {
                        clientIds.add(key);
                    }
                } else {
                    clientIds.remove(key);
                    countNum.addAndGet(1);
                }
            }
            if (StringUtils.isNotEmpty(request.getModule())) {
                ClientChannelKeys channelKeys = ClientChannelKeys.toClientChannelKeys(key);
                if (request.getModule().equals(channelKeys.getModule())) {
                    if (countNum.get() <= 0) {
                        clientIds.add(key);
                    }
                } else {
                    clientIds.remove(key);
                    countNum.addAndGet(1);
                }
            }
            if (StringUtils.isNotEmpty(request.getId())) {
                ClientChannelKeys channelKeys = ClientChannelKeys.toClientChannelKeys(key);
                if (request.getId().equals(channelKeys.getId())) {
                    if (countNum.get() <= 0) {
                        clientIds.add(key);
                    }
                } else {
                    clientIds.remove(key);
                    countNum.addAndGet(1);
                }
            }
            if (StringUtils.isNotEmpty(request.getUserId())) {
                ClientChannelKeys channelKeys = ClientChannelKeys.toClientChannelKeys(key);
                if (request.getUserId().equals(channelKeys.getUserId())) {
                    if (countNum.get() <= 0) {
                        clientIds.add(key);
                    }
                } else {
                    clientIds.remove(key);
                    countNum.addAndGet(1);
                }
            }
        });
        return clientIds;
    }


}
