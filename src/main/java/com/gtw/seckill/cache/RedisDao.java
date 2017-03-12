package com.gtw.seckill.cache;

import com.gtw.seckill.entity.Seckill;

public interface RedisDao {
    Seckill getSeckill(long seckillId);
    long putSeckill(Seckill seckill);
}
