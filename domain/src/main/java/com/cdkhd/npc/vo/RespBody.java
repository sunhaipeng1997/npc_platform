package com.cdkhd.npc.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

/**
 * 请求响应体
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RespBody<T> {

    //响应状态，默认为OK(200)
    private HttpStatus status = HttpStatus.OK;

    //说明信息
    private String message;

    //响应数据
    private T data;

    public HttpStatus getStatus() {
        return status;
    }

    public RespBody<T> setStatus(HttpStatus status) {
        this.status = status;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public RespBody<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public T getData() {
        return data;
    }

    public RespBody<T> setData(T data) {
        this.data = data;
        return this;
    }
}

