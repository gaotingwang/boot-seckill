package com.gtw.seckill.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gtw.seckill.entity.Seckill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Seckill> optRedisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, Seckill> redisTemplate = new RedisTemplate<String, Seckill>();
        redisTemplate.setConnectionFactory(cf);// 指定连接工厂
        setSerializer(redisTemplate);// 设置redis的key与value的序列化方式
        return redisTemplate;
    }

    /**
     * spring-data-redis对key和value 都进行了序列化 变成byte[]再调用对应的redis java client进行存储的
     */
    private void setSerializer(RedisTemplate<String, Seckill> redisTemplate){
        //设置key序列化方式
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        //设置value序列化方式
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
    }
}
