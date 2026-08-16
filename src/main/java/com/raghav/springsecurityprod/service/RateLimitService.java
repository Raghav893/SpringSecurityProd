package com.raghav.springsecurityprod.service;

import com.raghav.springsecurityprod.entity.RateLimitRule;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.redis.jedis.cas.JedisBasedProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class RateLimitService{
    private final JedisBasedProxyManager<String> proxyManager;

    private Bucket resolveBucket(String bucketKey, RateLimitRule rateLimitRule){
        Supplier<BucketConfiguration> configSupplier = () -> BucketConfiguration.builder()
                .addLimit(limit -> limit.capacity(rateLimitRule.capacity())
                        .refillIntervally(rateLimitRule.capacity(),rateLimitRule.window()))
                .build();
        return  proxyManager.getProxy(bucketKey,configSupplier);
    }
    /** Peek without consuming. */
    public boolean wouldPass(String bucketKey, RateLimitRule rule) {
        Bucket bucket = resolveBucket(bucketKey, rule);
        return bucket.getAvailableTokens() > 0;
    }

    /** Actually consume one token. */
    public boolean consume(String bucketKey, RateLimitRule rule) {
        Bucket bucket = resolveBucket(bucketKey, rule);
        return bucket.tryConsume(1);
    }
}
