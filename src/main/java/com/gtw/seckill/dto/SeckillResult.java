package com.gtw.seckill.dto;

import lombok.Data;

//将所有的ajax请求返回类型，全部封装成json数据
@Data
public class SeckillResult<T> {

    //请求是否成功
    private boolean success;
    private T data;
    private String error;

    public SeckillResult(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public SeckillResult(boolean success, String error) {
        this.success = success;
        this.error = error;
    }
}