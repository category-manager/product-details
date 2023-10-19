package com.github.sudarshan.productdetails.configs;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@EnableCaching
@Configuration
public class CacheConfig {
    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager("suggestions", "ordinal-words");
        caffeineCacheManager.setCaffeine(caffeine);
        return caffeineCacheManager;
    }
    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine
                .newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterWrite(60, TimeUnit.MINUTES);
    }
}
