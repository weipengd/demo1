package com.yxt.websocketsvc.ws;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.Data;

/**
 * @description:
 * @author: dongwp
 * @create: 2021/7/2
 **/
@Data
@Builder
public class WebSocketResponse {
    private int state;
    private String desc;

    public static String getStatus(State state) {
        WebSocketResponse build = WebSocketResponse.builder()
                .state(state.getCode())
                .desc(state.getValue())
                .build();
        return JSON.toJSONString(build);
    }
    public static String getSuccess(){
        return getStatus(State.SUCCESS);
    }

    public static String getFail(){
        return getStatus(State.FAIL);
    }

    public static String getTimeOut(){
        return getStatus(State.TIME_OUT);
    }

    public static String getInstanceMaxConnFail(){
        return getStatus(State.INSTANCE_MAX_CONN_FAIL);
    }

    public static String getHeartbeat(){
        return getStatus(State.HEARTBEAT);
    }
   public enum State {
        SUCCESS("连接成功", 0),
        FAIL("连接失败", 1),
        TIME_OUT("超时断开", 2),
        INSTANCE_MAX_CONN_FAIL("实例超出最大连接数",3),
       HEARTBEAT("心跳",4);

        State(String value, int code) {
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

