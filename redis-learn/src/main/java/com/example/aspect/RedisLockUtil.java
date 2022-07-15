package com.example.aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class RedisLockUtil {

    public static final Logger log = LoggerFactory.getLogger(RedisLockUtil.class);
    private static final long EXPIRE = 1000 * 10L;
    private static final long WAIT_TIME = 1000 * 10L;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * setnx + 时间
     * <pre>
     *     1. 过期时间是客户端生成的，分布式环境下每个客户端的时间需要同步
     *     2. 锁没有保存持有者标识，可能被其它客户端释放，也可能释放其它线程的锁 (过期之后可能代码的业务逻辑还没有完成)
     * </pre>
     */
    public boolean lock(String key, String value) {
        log.info("获取锁 key: {}, value: {}", key, value);
        long requestTime = System.currentTimeMillis();
        while (true) {
            // 等待锁时间超过了最大等待时间，则获取锁失败
            long waitTime = System.currentTimeMillis() - requestTime;
            if (waitTime > EXPIRE) {
                log.info("长时间没获取到锁，超时了 key: {}, value: {}", key, value);
                return false;
            }

            // 锁不存在，加锁成功后直接返回，使用 setnx 设置过期时间
            if (Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, String.valueOf(System.currentTimeMillis())))) {
                log.info("获取锁成功 key: {}, value: {}", key, value);
                // 设置超时时间，防止解锁失败，造成死锁
                redisTemplate.expire(key, EXPIRE, TimeUnit.MILLISECONDS);
                return true;
            }

            // 锁已经存在，判断它是否超时
            String valueTime = redisTemplate.opsForValue().get(key);
            if (!StringUtils.isEmpty(valueTime) && System.currentTimeMillis() - Long.parseLong(valueTime) > EXPIRE) {
                // 过期了，删除 key，防止死锁
                log.info("上一个锁超时，需要删除 key: {}, value: {}", key, value);
                redisTemplate.opsForValue().getOperations().delete(key);
            }

            try {
                // 获取锁失败，等待 20ms 后重试
                log.info("获取锁失败，等待 20ms 重试 key: {}, value: {}", key, value);
                TimeUnit.MILLISECONDS.sleep(WAIT_TIME);
            } catch (InterruptedException e) {
                log.error("等待获取锁时出现异常 key: {}, value: {}", key, value);
                e.printStackTrace();
            }
        }
    }

    /**
     * 分布式加锁
     */
    public boolean seckillLock(String key, String value) {
        if (redisTemplate.opsForValue().setIfAbsent(key, value)) {
            return true;
        }
        String currentValue = redisTemplate.opsForValue().get(key);
        if (!StringUtils.hasLength(currentValue)
                && Long.parseLong(currentValue) < System.currentTimeMillis()) {
            String oldValue = redisTemplate.opsForValue().getAndSet(key, value);
            if (!StringUtils.isEmpty(oldValue) || Objects.equals(oldValue, currentValue)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解锁，需要判断锁是不是当前线程加的 (不能释放其它线程的锁)
     */
    public void unlock(String key, String value) {
        // 判断 和 删除需要是原子操作，通过 lua 脚本完成

        // script 可以缓存起来
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/unlock.lua")));

        List<String> keys = List.of(key);
        Long execute = redisTemplate.execute(script, keys, value);
        if (execute == 1) {
            log.info("解锁成功 key: {}, value: {}", key, value);
        } else {
            log.info("解锁失败 key: {}, value: {}", key, value);
        }
    }
}
