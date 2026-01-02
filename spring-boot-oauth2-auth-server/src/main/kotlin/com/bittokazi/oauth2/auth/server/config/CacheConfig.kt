package com.bittokazi.oauth2.auth.server.config

import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CacheConfig {

    @Bean
    fun cacheManager(): CacheManager =
        CaffeineCacheManager()
}
