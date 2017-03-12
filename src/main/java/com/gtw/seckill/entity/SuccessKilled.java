package com.gtw.seckill.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author 高廷旺
 * 创建时间: 2017-03-05 22:17
 * 创建原因: 秒杀成功详情
 */
@Data
public class SuccessKilled {
    private long seckillId;
    private long userPhone;
    private short state;
    private Date createTime;
    //多对一,因为一件商品在库存中有很多数量，对应的购买明细也有很多。
    private Seckill seckill;
}
