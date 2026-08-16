package com.raghav.springsecurityprod.config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.serialization.Mapper;
import io.github.bucket4j.redis.jedis.Bucket4jJedis;
import io.github.bucket4j.redis.jedis.cas.JedisBasedProxyManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.RedisClient;

import java.time.Duration;

@Configuration
public class RedisConfig {


    @Bean
    public RedisClient redisClient(){
        return RedisClient.builder()
                .hostAndPort("localhost",6379)
                .build();

    }
    @Bean
    public JedisBasedProxyManager<String> proxyManager(RedisClient redisClient){
        return Bucket4jJedis.casBasedBuilder(redisClient)
                .keyMapper(Mapper.STRING).expirationAfterWrite(
                        ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(15))
                )
                .build();
    }

}
