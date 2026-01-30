package com.livk.admin.server.config;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.eventstore.InstanceEventPublisher;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static java.util.Comparator.comparing;

/**
 * <p>
 * RedisEventStore
 * </p>
 *
 * @author livk
 * @date 2026/1/27
 */
@Slf4j
@Component
public class RedisEventStore extends InstanceEventPublisher implements InstanceEventStore {

	private static final String INSTANCE_EVENT_KEY = "Event";

	private static final Comparator<InstanceEvent> byTimestampAndIdAndVersion = comparing(InstanceEvent::getTimestamp)
		.thenComparing(InstanceEvent::getInstance)
		.thenComparing(InstanceEvent::getVersion);

	private final ReactiveHashOperations<String, String, List<InstanceEvent>> hashOperations;

	public RedisEventStore(ReactiveRedisTemplate<String, Object> redisTemplate) {
		this.hashOperations = redisTemplate.opsForHash();
	}

	@NonNull
	@Override
	public Flux<InstanceEvent> findAll() {
		return hashOperations.values(INSTANCE_EVENT_KEY)
			.flatMapIterable(Function.identity())
			.sort(byTimestampAndIdAndVersion);
	}

	@NonNull
	@Override
	public Flux<InstanceEvent> find(@NonNull InstanceId id) {
		return hashOperations.get(INSTANCE_EVENT_KEY, id).flatMapMany(Flux::fromIterable);
	}

	@NonNull
	@Override
	public Mono<Void> append(List<InstanceEvent> events) {
		if (events.isEmpty()) {
			return Mono.empty();
		}
		var id = events.getFirst().getInstance();
		if (!events.stream().map(InstanceEvent::getInstance).allMatch(id::equals)) {
			throw new IllegalArgumentException("events must only refer to the same instance.");
		}
		return hashOperations.put(INSTANCE_EVENT_KEY, id.getValue(), events)
			.then(Mono.fromRunnable(() -> this.publish(events)));
	}

}
