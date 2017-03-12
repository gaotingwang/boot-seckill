package com.gtw.seckill.service;

import com.gtw.seckill.dto.Exposer;
import com.gtw.seckill.dto.SeckillExecution;
import com.gtw.seckill.entity.Seckill;
import com.gtw.seckill.exception.RepeatKillException;
import com.gtw.seckill.exception.SeckillCloseException;
import com.gtw.seckill.exception.SeckillException;

import java.util.List;

/**
 * 业务接口:站在使用者(程序员)的角度设计接口
 * 三个方面:
 * 1.方法定义粒度，方法定义的要非常清楚
 * 2.参数要越简练越好
 * 3.返回类型(return 类型一定要友好/或者return异常，我们允许的异常)
 */
public interface SeckillService {

    /**
     * 查询全部的秒杀记录
     */
    List<Seckill> getSeckillList();

    /**
     * 查询单个秒杀记录
     */
    Seckill getById(long seckillId);

    /**
     * 在秒杀开启时输出秒杀接口的地址，否则输出系统时间和秒杀时间
     */
    Exposer exportSeckillUrl(long seckillId);

    /**
     * 执行秒杀操作，有可能失败，有可能成功，所以要抛出我们允许的异常
     */
    SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws RepeatKillException, SeckillCloseException, SeckillException;

    /**
     * 使用存储过程的方式进行秒杀
     */
    SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5);
}
