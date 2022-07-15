package com.example;

import com.example.pojo.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class RedisTest {

    private static final Logger log = LoggerFactory.getLogger(RedisTest.class);

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Test
    public void operation() {
        // 获取 redis 连接对象
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        connection.flushDb();

        // 操作不同数据类型
        // opsForValue 字符串
        // opsForList
        // opsForHash
        // opsForSet

        redisTemplate.opsForValue().set("k1", "v1");
        Object v1 = redisTemplate.opsForValue().get("k1");
        log.info(String.valueOf(v1));

    }

    @Test
    public void serialize() throws JsonProcessingException {
        User user = User.UserBuilder.anUser().age(10).username("qqq").password("www").build();
        String json = new ObjectMapper().writeValueAsString(user);

        // set 的对象需要序列化
        redisTemplate.opsForValue().set("user", json);
        Object o = redisTemplate.opsForValue().get("user");
        log.info(o.toString());
    }

    public static void main(String[] args) throws JsonProcessingException {
        Jedis jedis = new Jedis("localhost", 6379);
        String ping = jedis.ping(); // PONG

        String flushDB = jedis.flushDB();
        boolean existsK1 = jedis.exists("k1");
        String setUsername = jedis.set("username", "qqq");

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> json = new HashMap<>();
        json.put("k1", "v1");
        json.put("k2", "v2");
        String s = mapper.writeValueAsString(mapper);


        // 事务操作
        Transaction transaction = jedis.multi();
        try {
            transaction.set("user1", s);
            transaction.set("user2", s);
            // 执行事务
            transaction.exec();
        } catch (Exception e) {
            // 放弃事务
            String discard = transaction.discard();
            e.printStackTrace();
        } finally {
            log.info(jedis.get("user1"));
            log.info(jedis.get("user2"));
            jedis.close();
        }

    }
}
