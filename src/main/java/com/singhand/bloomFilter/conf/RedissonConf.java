package com.singhand.bloomFilter.conf;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class RedissonConf{

    @Bean
    public RedissonClient redissonClient() throws IOException {
        ClassPathResource redisConfResource = new ClassPathResource("redis-conf.yml");
        Config config = Config.fromYAML(redisConfResource.getInputStream());
        return Redisson.create(config);
    }

}
