package com.example;

import com.alibaba.fastjson.JSONObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

public class RedisTest {

    public static void main(String[] args) {
        Jedis jedis = new Jedis("localhost", 6379);
        String ping = jedis.ping(); // PONG

        String flushDB = jedis.flushDB();
        boolean existsK1 = jedis.exists("k1");
        String setUsername = jedis.set("username", "qqq");

        JSONObject json = new JSONObject();
        json.put("k1", "v1");
        json.put("k2", "v2");
        String s = json.toJSONString();

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
            jedis.close();
        }

    }
}
