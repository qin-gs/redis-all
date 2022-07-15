package com.example;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RedisTemplateTest {

    private static final Logger log = LoggerFactory.getLogger(RedisTemplateTest.class);

    RedisTemplate<String, Object> redisTemplate = getRedisTemplate();

    /**
     * 字符串相关操作
     */
    @Test
    public void str() {
        // 普通操作
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        // 设置
        ops.set("k1", 3);
        // 获取
        Object v1 = ops.get("k1");
        // 删除
        redisTemplate.delete("k1");
        // 递增
        ops.increment("k1", 1);

        // 通过 BoundValueOperations 操作
        BoundValueOperations<String, Object> key = redisTemplate.boundValueOps("key");
        // 设置值的同时设置过期时间
        key.set("value", 1, TimeUnit.MINUTES);
        // 获取值
        Object o = key.get();
        key.increment(3);
        // 单独设置过期时间
        key.expire(2, TimeUnit.SECONDS);
    }

    /**
     * hash 相关操作
     */
    @Test
    public void hash() {
        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();
        // 放进去
        ops.put("h1", "k1", "v1");
        ops.putAll("h2", Map.of("k2", "v2", "k3", "v3"));
        // 获取 hash 中某个 key 的值
        ops.get("h1", "k1");
        // 获取 hash 中的键值对集合
        Map<Object, Object> entries = ops.entries("h1");
        // 删除
        Long delete = ops.delete("h1", "k1");
        // 判断 hash 中是否存在某个 key
        ops.hasKey("h1", "k1");

        // 设置过期时间需要 valueOps
        redisTemplate.boundValueOps("h1").expire(1, TimeUnit.MINUTES);

        // 获取 hash 的所有 key
        Set<Object> keys = ops.keys("h2");
        log.info("keys: {}", keys);
        // 获取 hash 的所有 value
        List<Object> values = ops.values("h2");
        log.info("values: {}", values);

        // 获取对指定名称的 hash 的所有操作
        BoundHashOperations<String, Object, Object> hash = redisTemplate.boundHashOps("hash");
        hash.put("k1", "v1");
        hash.put("k2", "v2");
        hash.get("k1");
        hash.expire(1, TimeUnit.MINUTES);
        hash.entries();
        hash.delete("k1");
        hash.hasKey("k1");
    }

    /**
     * 执行 lua 脚本
     */
    @Test
    public void lua() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/lock.lua")));

        List<String> keys = List.of("k1", "k2");
        Long execute = redisTemplate.execute(script, keys, 10, 20);
        System.out.println("execute = " + execute);
    }

    public RedisTemplate<String, Object> getRedisTemplate() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration("localhost", 6379);
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(configuration);
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // string 序列化
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // key 和 hash.key 采用 string 的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        // value 采用 jackson 序列化
        template.setValueSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class));
        template.setConnectionFactory(jedisConnectionFactory);
        template.afterPropertiesSet();
        return template;
    }
}
