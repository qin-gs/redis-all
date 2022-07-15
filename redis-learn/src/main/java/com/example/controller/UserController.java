package com.example.controller;

import com.example.anno.RedisLockAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	final String REDIS_LOCK = "REDIS_LOCK_";

	@PostMapping("/add")
	@RedisLockAnnotation(redisKey = REDIS_LOCK + "#0")
	public Object updateUser(@RequestParam String id, @RequestParam String userName) {
		logger.info("updateUser id:{}, userName:{}", id, userName);
		return "update user success";
	}
}
