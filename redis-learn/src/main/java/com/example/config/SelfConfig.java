package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

/**
 * RedisTemplate 的具体配置
 */
@Configuration
@Conditional(OsCondition.class)
public class SelfConfig {

    /**
     * 连接池配置
     */
    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(100);
        jedisPoolConfig.setMaxIdle(10);
        jedisPoolConfig.setMinIdle(1);
        jedisPoolConfig.setMaxWaitMillis(1000);
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setTestOnReturn(true);
        jedisPoolConfig.setTestWhileIdle(true);
        return jedisPoolConfig;
    }

    /**
     * 创建工厂，配置连接信息
     */
    @Bean
    public JedisConnectionFactory jedisConnectionFactory(JedisPoolConfig jedisPoolConfig) {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration("localhost", 6379);

        JedisClientConfiguration.JedisPoolingClientConfigurationBuilder builder = JedisClientConfiguration.builder().usePooling();
        builder.poolConfig(jedisPoolConfig);

        return new JedisConnectionFactory(configuration, builder.build());
    }

    /**
     * 创建 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(JedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        // string 序列化
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // key 和 hash.key 采用 string 的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        // value 采用 jackson 序列化
        template.setValueSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class));

        template.setConnectionFactory(jedisConnectionFactory);
        return template;
    }
}
