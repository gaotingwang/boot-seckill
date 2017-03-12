package com.gtw.seckill.cache;

import com.gtw.seckill.entity.Seckill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class RedisDaoImpl implements RedisDao{
    @Autowired
    private RedisTemplate<String, Seckill> redisTemplate;

    public Seckill getSeckill(long seckillId) {
        log.info("从redis中获取");
        return redisTemplate.opsForValue().get(seckillId + "");
    }

    public long putSeckill(Seckill seckill) {
        log.info("保存在redis中");
        ValueOperations<String, Seckill> operations = redisTemplate.opsForValue();
        operations.set(seckill.getSeckillId() + "", seckill);
        return seckill.getSeckillId();
    }
}
