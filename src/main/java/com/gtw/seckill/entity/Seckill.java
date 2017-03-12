package com.gtw.seckill.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author 高廷旺
 * 创建时间: 2017-03-05 22:17
 * 创建原因: 秒杀库存对象
 */
@Data
public class Seckill {
    private long seckillId;
    private String name;
    private int number;
    private Date startTime;
    private Date endTime;
    private Date createTime;
}
