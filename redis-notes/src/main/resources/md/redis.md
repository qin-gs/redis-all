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

rdb 文件生成条件 (config get dir 查看持久化文件位置)，启动时自动恢复

- save 条件满足
- 执行 flushall 命令
- 退出 redis

适合大规模数据恢复

对数据完整性要求不高

有一定的时间间隔，会丢一些数据

fork 进程的时候会占用一些内存
