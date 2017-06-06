package com.girl.common;

/**
 * 响应状态码的枚举类
 *
 * Created by girl on 2017/5/30.
 */
public enum  ResponseCode {

    SUCCESS(0, "SUCCESS"),
    ERROR(1, "ERROR"),
    NEED_LOGIN(10, "NEED_LOGIN"),// 需要登录
    ILLEGAL_ARGUMENT(2, "参数错误");// 非法参数

    private final int code;// 响应状态码
    private final String desc;// 描述

    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
