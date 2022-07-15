package com.example.aspect;

import com.example.anno.RedisLockAnnotation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RedisLockAspect {
	private static final Logger log = LoggerFactory.getLogger(RedisLockAspect.class);

	@Autowired
	private RedisLockUtil redisLockUtil;

	@Around("@annotation(redisAnnotation)")
	public Object redisLock(ProceedingJoinPoint point, RedisLockAnnotation redisAnnotation) {
		String lockKey = null;
		boolean flag = false;

		String parameterIndex = redisAnnotation.redisKey().substring(redisAnnotation.redisKey().indexOf('#') + 1);
		int index = Integer.parseInt(parameterIndex);
		// 获取注解方法的参数列表
		Object[] args = point.getArgs();
		// 生成 redis key
		lockKey = redisAnnotation.redisKey().replace("#" + parameterIndex, args[index].toString());
		log.info("redis key: {}", lockKey);

		// 将 redis key 放入 redis
		flag = redisLockUtil.lock(lockKey, args[index].toString());
		log.info("redis lock: {}", flag);

		try {
			// 如果执行太长时间，redis 中的锁可能会过期
			// 其它线程可能会拿到锁，改线程可能会释放不属于自己的锁
			return point.proceed();
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			// 需要判断 redis 中的锁是不是当前线程加的，是的话才能释放
			if (flag) {
				redisLockUtil.unlock(lockKey, null);
			}
		}

		return null;
	}
}
