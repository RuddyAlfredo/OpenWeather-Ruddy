
package com.bootcamp.clima.security.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;

import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.sf.ehcache.config.CacheConfiguration;

@Configuration
@EnableCaching
public class ChacheConfiguration extends CachingConfigurerSupport{

    @Bean
    public net.sf.ehcache.CacheManager ehCacheManager (){
        CacheConfiguration config = new CacheConfiguration();
        config.setName("consultasCache");
        config.setMemoryStoreEvictionPolicy("LFU");
        config.setMaxEntriesLocalHeap(100);
        config.setTimeToLiveSeconds(60);
           
        net.sf.ehcache.config.Configuration con = new net.sf.ehcache.config.Configuration();
        con.addCache(config);
        return net.sf.ehcache.CacheManager.newInstance(con);
    }
    
    @Bean
    public CacheManager cacheManager (){
        return new EhCacheCacheManager(ehCacheManager());
    }
}
