/*
 * Copyright 2021-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.livk.caffeine.handler;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author livk
 */
@Component
@RequiredArgsConstructor
public class CacheHandlerAdapter implements CacheHandler<Object> {

	private final RedisTemplate<String, Object> redisTemplate;

	private final Cache<String, Object> cache;

	@Override
	public void put(String key, Object proceed) {
		redisTemplate.opsForValue().set(key, proceed);
		cache.put(key, proceed);
	}

	@Override
	public void delete(String key) {
		cache.invalidate(key);
		redisTemplate.delete(key);
	}

	@Override
	public Object read(String key) {
		return cache.get(key, s -> redisTemplate.opsForValue().get(s));
	}

	@Override
	public void clear() {
		var keys = cache.asMap().keySet();
		cache.invalidateAll();
		redisTemplate.delete(keys);
	}

}
