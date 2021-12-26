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





