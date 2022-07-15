### Redis



分库分表，水平拆分，集群



#### 什么是 NoSql

一些数据不需要固定的结构

```
传统的数据库
- 结构化组织
- sql
- 数据和关系都存在单独的表
- 严格的一致性
- 事务

nosql
- 没有固定的查询语言
- 键值对存储，列存储，文档存储，图形数据库
- 最终一致性
- cap定理 base
- 高性能，高可用，高可扩展性

大数据时代
海量，多样，实时
高并发，高扩展，高性能
```

```
商品基本信息
  mysql, oracle

商品描述，评论
  文档型数据库 mongodb

图片
  hdfs, fastdfs

商品关键字
  elastricsearch

商品热门波动信息
  内存数据库 redis, memcache

```



#### NoSql 分类

1. 键值对：redis, memcache, tair

2. 文档型：mongodb, couchdb

3. 列存储：Hbase, hdfs

4. 图关系(社交网络关系)：neo4j, infogrid



方便扩展

大数据量高性能



#### redis 入门

远程字典服务(remote dictionary server)

- 内存存储，数据持久化
- 高速缓存
- 发布订阅系统
- 计时器，计数器(浏览量)



docker 运行



启动Redis容器，并将Redis配置文件映射到本地conf文件夹

```
docker run -p 6379:6379 --name redis -v /Users/qgs/Desktop/docker/redis/conf/redis.conf:/etc/redis/redis.conf -v /Users/qgs/Desktop/docker/redis/data:/data -d redis redis-server /etc/redis/redis.conf --appendonly yes

docker run -p 6379:6379 -d redis // 简单启动
docker exec -it d06fa7355b89 /bin/bash // 进入启动后的界面

redis-server /conf/redis.conf
redis-server /etc/redis/redis.conf

// 进入之后
redis-cli -p 6379
// 退出
shutdown

// 移除容器
docker rm -f $(docker ps -aq)

// 性能测试
redis-benchmark -h localhost -p 6379 -c 100 -n 100000 -d 3
```



#### 基础知识



默认有 16 个数据库

```bash
select 3
set name qqq // 设置值
dbsize
get name // 获取已设置的值
append name '--' // 添加字符串
keys *
flushdb // 清空当前数据库
flushdball
exists name // 判断是否存在
move name 1 // 移除值
expire name 10 // 设置过期时间 10s
ttl name // 查看剩余时间
type name // 查看类型

```



**单线程**

避免上下文切换



#### 数据类型

- 字符串 strings
- 散列 hashes
- 列表 lists
- 集合 sets
- 集合有序集合 sorted sets



1. 字符串

   ```bash
   
   set name qqq # 设置值
   get name # 获取已设置的值
   append name '--' # 添加字符串，如果不存在相当于 set
   getrange name start end # 截取
   setrange name 2 xx # 替换
   keys *
   exists name # 判断是否存在
   
   setex k 20 'hello' # (set with expire)
   setnx k 'world' # (set not exists)
   
   mset k1 v2 k2 v2 # 一次设置多个值
   mget k1 k2 # 一次获取多个值
   msetnx k1 v1 k2 v2 # 原子操作，一个key存在，全部设置失败
   
   mset user:1:name n user:1:age 12 # 设置对象
   mget user:1:name user:1:age # 获取对象值
   
   getset key value # 不存在返回 nil，存在返回旧值，设置新值
   
   
   # 计数
   set counter 0
   incr counter # 递增
   incr by counter 10 # 递增指定数值
   decr counter # 递减
   decrby counter 10 # 递减指定数值
   ```

   

2. 列表 (实际是一个链表)

   ```bash
   lpush list 1
   lpush list 2
   lrange list 0 1
   rpush list 3
   
   lpop list
   rpop list
   
   lindex list 1 # 通过下标获取
   llen list # 获取长度
   lrem list 1 'hello' # 移除指定次数的元素
   ltrim list 1 2 # 通过下标截取指定长度 (包含首尾，原列表会被改变)
   rpoplpush list newlist # 将 list 中的最后一个元素放到 newlist 中
   exists list # 判断是否存在
   lset list 0 'world' # 替换指定下标的值(必须要存在，否则会报错)
   linsert list before/after 'hello' 'world' # 在 hello 之前/后插入 world
   ```



3. 集合

   ```bash
   sadd set 'hello' # 添加元素
   spop set # 随机移除元素
   smembers set # 查看集合中所有元素
   sismember set 'hello' # 判断是否存在
   scard set # 查看数量
   srem set 'hello' # 移除指定元素
   srandmember set 2 # 在集合中随机选择指定个数的元素(不给默认为1个)
   smove source destination member # 移动元素
   
   sdiff set1 set2 # 差集
   sinter set1 set2 # 交集
   sunion set1 set2 # 并集
   ```



4. 哈希

   ```bash
   hset hash k1 v1 k2 v2 # 添加元素
   hget hash k2 # 获取元素
   hmset hash k1 v1 k2 v2 # 设置多个值 废弃
   
   hdel hash k1 # 删除指定key
   hgetall hash # 获取全部元素 键+值
   hlen hash # 元素数量
   hexists hash k1 #是否存在
   
   hkeys hash # 只获取key
   hvals hash # 只获取value
   
   hincrby hash k1 1 # 给指定值加1
   hsetnx hash k3 v3 # 不存在才能设置
   ```

   

5. 有序集合

   ```bash
   zadd azset 1 one # 向 azset 中添加元素
   zrem azset onw # 移除元素
   zcard azset # 查看集合中元素个数
   
   zrange azset 0 -1 # 查看所有元素 正序
   zrevrange azset 0 -1 # 查看所有元素 逆序
   
   zrangebyscore azset -inf +inf # 排序(从负无穷 到 正无穷)
   zrangebyscore azset -inf 2500 withscores # 排序(只显示 负无穷 到 2500) 加上值
   
   zcount azset 1 3 # 计数
   ```

   

6. 地理位置

   ```bash
   geoadd cities 13.361389 38.115556 "Palermo"  # 将指定的地理空间位置（经度、纬度、名称）添加到指定的key中
   geodist cities Palermo Catania km # 获取两个给定位置间的距离(单位: km)
   geohash cities Palermo # 返回一个或多个位置元素的 Geohash 表示
   geopos cities Palermo # 获取指定地区的经纬度
   georadius cities 15 37 2000 km WITHDIST count 2 # 获取与指定位置距离不超过给定最大距离的所有位置元素
   georadiusbymember # 找出位于指定元素指定距离内的元素
   ```



7. 基数 (hyperloglog)

   ```bash
   pfadd pf a b c d d # 添加元素到 hyperloglog
   pfcount pf # 基数统计
   pfmerge all b c # 合并 b c 到 all
   ```

   

8. 位图 (bitmaps)

   ```bash
   setbit symbol Monday 1 # 设置(只有两个状态01)
   getbit symbol 0 # 获取状态
   bitcount symbol # 统计状态为1的数量
   ```



#### 事务

不保证原子性，没有隔离级别的概念

```bash
multi # 开启事务
set k1 v1
set k2 v2 
exec # 执行所有操作 (如果有错误命令，所有命令都不会执行；如果命令正确但是操作错误，其他会正常执行)
discard # 放弃事务队列中的所有操作
```

实现乐观锁

watch 监控某个值，事务执行的时候如果其他线程修改了这个值，事务会执行失败；

unwatch 放弃监控



#### SpringBoot

org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration



#### 配置文件

```bash
config set requirepass '123456' # 设置密码
config get requirepass # 获取密码

auth 123456 # 认证登录
```



```bash
单位 不区分大小写

1k => 1000 bytes
1kb => 1024 bytes
1m => 1000000 bytes
1mb => 1024*1024 bytes
1g => 1000000000 bytes
1gb => 1024*1024*1024 bytes

include /path/to/local.conf

include /path/to/other.conf # 可以包含多个配置文件

bind 127.0.0.1 # ip 绑定
protected-mode yes # 包含模式
port 6379 # 端口

deamonize yes # 以守护进程方式运行
pidfile /var/run/redis_6379.pid # 如果已后台方式运行，需要指定 pid 文件

loglevel notice # 日志
logfile "a.log" # 日志文件位置
databases 16 # 数据库数量
always-show-logo yes # 展示 logo

save 900 1 # 900s 内修改一次则进行保存

stop-write-on-bgsace-error yes # 持久化出错，是否继续
rdpcompression yes # 是否压缩 rdb 文件
rdbchecksum yes # 保存 rdb 文件时进行错误检查
dir ./ # rdb 文件位置

maxclients 10000 # 客户端最大数量
maxmemory 1000bytes # redis 配置最大内存容量
maxmemory-policy noeviction # 内存达到上限后的处理策略
  volatile-lru -> Evict using approximated LRU, only keys with an expire set.
  allkeys-lru -> Evict any key using approximated LRU.
  volatile-lfu -> Evict using approximated LFU, only keys with an expire set.
  allkeys-lfu -> Evict any key using approximated LFU.
  volatile-random -> Remove a random key having an expire set.
  allkeys-random -> Remove a random key, any key.
  volatile-ttl -> Remove the key with the nearest expire time (minor TTL)
  noeviction -> Don't evict anything, just return an error on write operations.

appendonly no # aof 模块
appendfilename 'aof.log' # 持久化文件名
appendsync always/everysec/no # 每次修改/每秒/从不 sync
```



#### 持久化

**rdb** (默认)

文件生成条件 (config get dir 查看持久化文件位置)，启动时自动恢复

- save 条件满足
- 执行 flushall 命令
- 退出 redis

适合大规模数据恢复

对数据完整性要求不高

有一定的时间间隔，会丢一些数据

fork 进程的时候会占用一些内存



**aof**

默认不开启，需要修改配置文件 appendonly yes

可以使用 redis-check-aof 文件修复 aof 文件 `redis-check-aof --fix appenfonly.aof`

每一次修改都进行同步，文件完整性更好

每秒同步一次，可能会丢失一秒的数据

aof 生成的文件大于 rdb，修复速度慢(但是如果同时开启两种持久化方式，优先使用 aof 恢复(更完整))

aof 运行效率低



#### 发布订阅

发布 publish

频道 channel

定义 subscribe

```bash
subscribe channel-1 # 订阅频道

publish channel-1 "a message" # 发布消息
接收的是消息
消息来自哪个频道
消息内容
```



#### 主从复制

数据复制是单向的( 主 -> 从 )

作用：

- 数据冗余
- 故障恢复
- 负载均衡( 读写分离 )
- 高可用

搭建集群需要修改 redis.conf 配置文件

- port 端口
- daemonize yes 后台运行
- pidfile
- logfile
- dbfilename

```bash
redis-server conf/redis-6379.conf # 启动三个
ps -ef | grep redis # 看一下进程
# 默认都是主节点，需要将其中两个修改成从节点
slaveof 127.0.0.1 6379 # 将 6379 作为主节点
info replication # 查看信息

# 修改配置文件
replicaof 127.0.0.1 6379
masterauth password
```

全量复制 / 增量复制



#### 哨兵模式

主节点故障后自动切换，节点恢复后作为从节点

哨兵配置文件 sentinel.conf

```bash
# sentinel monitor 被监控的主机名称 host port 几个从节点认为主节点失联后从新选举
sentinel monitor redis-sentinel 127.0.0.1 6379 1

redis-sentinel sentinel.conf # 启动哨兵
```



#### 缓存穿透/缓存雪崩

缓存穿透 (查不到)

缓存击穿 (缓存过期，过期之后请求量太大)

缓存雪崩 (某段时间缓存集中失效)

- redis 高可用
- 限流降级
- 数据预热





#### 分布式锁

- **互斥性**: 任意时刻，只有一个客户端能持有锁。
- **锁超时释放**：持有锁超时，可以释放，防止不必要的资源浪费，也可以防止死锁。
- **可重入性**:一个线程如果获取了锁之后,可以再次对其请求加锁。
- **高性能和高可用**：加锁和解锁需要开销尽可能低，同时也要保证高可用，避免分布式锁失效。
- **安全性**：锁只能被持有的客户端删除，不能被其他客户端删除





1. redis 实现分布式锁的原理是什么？

   A：锁本质上是一种逻辑控制，使用一个布尔型的变量就可以。比方说，可以让 Redis 中的某个键存在表示上了某种锁，当 Redis 中没有这个键时表示没有上这个锁。

   而 Redis 是独立于用户程序的一种拥有集群功能的全局分布式应用，因此可以用于实现分布式锁。

2. Q：如何实现 Redis 分布式锁的线程级可重入。

   可以使用 ThreadLocal 记录每个线程当前上锁的重入次数。每当上锁时，就将记录中的重入次数加 1。每当释放锁时，就将其减 1。特别地，在释放锁时，如果重入次数为 1，就真正地在 Redis 中删除此锁。

3. 对于这种情况如何应对：一个程序在设置了 Redis 分布式锁之后，然后业务代码中抛出了异常，结果程序跳过了后面的释放锁代码就退出了。

   可以将加分布式锁的代码置于一个 try 块 中，然后在 try 块 后面加不含 catch 块 的 finally 子句，并在 finally 子句 中编写释放锁的代码。这样，无论中途发生了什么异常，释放锁的代码一定会执行。

4. 在问题【3】中，如果一个程序在没有获得锁的情况下就退出，这不就可能会释放正在持有锁的程序的锁吗？

   对于这种情况可以借助 ThreadLocal，用两种方法来应对：

   使用 ThreadLocal 为每个线程生成一个 ID，然后将此 ID 存于 Redis 锁中，等释放锁之时，检查锁中的 ID 与本线程的 ID 是否一致。如果一致才真正释放锁。

   利用本 Redis 锁的互斥性。使用 ThreadLocal 记录每个线程当前上锁的重入次数。因为本 Redis 锁是互斥锁，所以只可能有一个线程，它的当前上锁次数大于 0。因此，释放锁的时候只需要判断自己当前的上锁次数是否为 0 即可。如果不为 0，才真正释放锁。

   本文使用的是这种方法。

5. 对于这种情况如何应对：一个程序在设置了 Redis 分布式锁之后，还没来得及释放该锁就崩溃了。此时，所有的程序都无法获取受该锁束缚的资源。

   可以选择在上锁的同时引入超时时间。此时如果问题中的程序崩溃时，锁会自动释放。

6. 在问题【5】中，如果该程序在上锁之后还没有来得及设置超过时间就崩溃呢？

   可以让上锁和设置超过时间这两个操作变成同一个原子操作。

   现在，Spring Boot 中的 RedisTemplate 有这种 API 可以实现这一点。

   如果有的技术没有提供这种 API，可以使用 Redis 中的 Eval 命令，这个命令支持运行一段 Lua 脚本，这个命令是原子性的。

【错误的解决方案】

Q1：在问题【5】中，如果该程序在上锁之后还没有来得及设置超过时间就崩溃呢？

A1：可以将本次上锁时间作为 Redis 锁的值存入，同时规定某个键存在表示上了某种锁，没有这个键时表示没有上这个锁。然后令读取锁的程序通过比较上锁时间与当前时间来判断此锁有没有过期。

Q2：如果锁过期了，如何保证只有一个程序可以获得锁？

A2：可以使用类似于乐观锁的机制，在上锁时同时将上锁应用的 ID 存入，然后在加锁之后再读取锁数据，判断最后加锁成功的是不是自己即可。

Q3：要怎么做到对“最后加锁”的判断？如何解决这种情况：两个程序都要加锁，而第一个程序执行很快，加锁之后又认为自己成功加上了锁。然后第二个执行较慢的程序将锁覆盖，也认为自己成功加上了锁。现在，两个程序都认为自己加上了锁。

A3：这确实是错误的解决方案。







7. 在问题【5】中，如果该程序在上锁后业务代码执行时间过长而锁超时怎么办？

   可以在加锁之后开启一个子线程进行异步周期性地续时。当释放锁时，再中断结束这个续时线程。

8. 在问题【7】中，每次上锁都开启新线程，这个开销是不是有点大了？

   那可以选择让同一个名称的锁对应同一个续时线程。具体来说，事先开启一个续时线程，这个续时线程不会因锁释放而销毁。然后让这个续时线程完成所有线程上锁的续时任务。

9. 在问题【8】中，如果程序需要使用 1w 个锁来锁 1w 条不同的数据，那这样在后台开启 1w 个续时线程是不是容易溢出？

   可以在创建续时线程时设置续时线程的个数上限。如果达到上限，可以采取很多策略，比如令新的续时线程像问题【7】一样在锁释放时销毁。

10. 问一个与创建 Redis 分布式锁无关的问题。对于秒杀的业务，假设购买商品前要加锁，如果没有拿到锁，会自旋等待。现在如果有 1w 个购买请求，但商品数只有 100 个，这就意味着理论上在秒杀结束之后，有 9900 个请求是不需要拿到锁的。如何保证这一点？如何防止这样的一种情况：明明秒杀已经结束了，剩下的 9900 个请求仍然在自旋排队拿锁，并在拿到锁之后执行业务代码。

    如果这个秒杀项目使用了一种高速缓存技术，可以选择在秒杀结束之后，将秒杀结束这一信号存于高速缓存中。当请求在自旋等待时，不断在高速缓存中查询秒杀是否结束，如果是就结束自旋。同时在拿到锁之后，也要查询秒杀是否结束，如果是就跳过某些业务代码。

11. 在问题【10】中，为什么在拿到锁之后，也要查询秒杀是否结束？

    在线程在自旋等待过程中，其可能会位于自旋等待过程中的任何一个时间点。如果有大量的线程位于拿锁的时间点，那么当其它其它线程释放锁时，即便是秒杀结束了，自旋等待中判断秒杀是否结束的代码也不会起作用。因为当它拿到锁的时候，就会马上退出循环，而不会经历这个自旋中的判断代码。因此在拿到锁之后，也要执行这个判断代码。





【编程难点】（这些问题的答案不方便文字描述，这里从略。读者可以在文末笔者的源代码中找到解决方案）

在规定一个分布式锁对应一个续时线程的情况下，如果需要使用多个锁，如何避免多线程并发时，为每一个锁创建了多个续时线程？

如何在多线程共用同一续时线程的情况下，控制此续时线程的续时停止与恢复？

如何保证在得到和释放分布式锁时，续时线程能立刻感知到？（如果续时线程刚好在休眠，那它就不能立刻感知到）

如何防止续时线程意外中止？



