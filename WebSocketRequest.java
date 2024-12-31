package com.yxt.websocketsvc.ws;

import com.yxt.websocketsvc.utils.JwtTokenUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: websocket请求参数
 * @author: dongwp
 * @create: 2021/6/22
 **/
@Data
public class WebSocketRequest implements Serializable {
    /**
     * token参数用来鉴权，
     */
    private String jwt;
    /**
     * 项目类型区分
     */
    private String project;
    /**
     * 项目模块类型
     */
    private String module;
    /**
     * 业务id
     */
    private String id;
    /**
     * 实例id，一个用户可以建立多个连接
     */
    private String instanceId;
    /**
     * 消息id，扩展后期跟踪请求
     */
    private String messageId;
    /**
     * 消息类型：比如心跳
     */
    private String type; //heartbeat
    /**
     * 扩展的内容体
     */
    private String body;
    /**
     * 消息创建时间，用来做消息发送到接收时间统计
     */
    private String createTime; //2021-07-05 16:09:58

    public String getUserId() {
        if (jwt == null) {
            return "";
        }
        return JwtTokenUtil.verifyToken(jwt);
    }

    public enum Type {
        HEARTBEAT("heartbeat", 4);

        Type(String value, int code) {
            this.value = value;
            this.code = code;
        }

        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }


}
