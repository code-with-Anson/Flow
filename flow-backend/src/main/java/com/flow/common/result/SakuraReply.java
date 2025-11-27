package com.flow.common.result;

import com.flow.common.enums.ErrorCode;
import lombok.Data;
import java.io.Serializable;

@Data
public class SakuraReply<T> implements Serializable {
    private int code;
    private String message;
    private T data;
    private long timestamp;

    public SakuraReply() {
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> SakuraReply<T> success(T data) {
        SakuraReply<T> reply = new SakuraReply<>();
        reply.setCode(ErrorCode.SUCCESS.getCode());
        reply.setMessage(ErrorCode.SUCCESS.getMessage());
        reply.setData(data);
        return reply;
    }

    public static <T> SakuraReply<T> success() {
        return success(null);
    }

    public static <T> SakuraReply<T> error(int code, String message) {
        SakuraReply<T> reply = new SakuraReply<>();
        reply.setCode(code);
        reply.setMessage(message);
        return reply;
    }

    public static <T> SakuraReply<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMessage());
    }

    public static <T> SakuraReply<T> error(ErrorCode errorCode, String message) {
        return error(errorCode.getCode(), message);
    }
}
