package com.livk.admin.server.config;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.notify.AbstractEventNotifier;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * <p>
 * CustomNotifier
 * </p>
 *
 * @author livk
 * @date 2026/1/27
 */
@Slf4j
@Component
public class CustomNotifier extends AbstractEventNotifier {

	protected CustomNotifier(InstanceRepository repository) {
		super(repository);
	}

	@NonNull
	@Override
	protected Mono<Void> doNotify(@NonNull InstanceEvent event, @NonNull Instance instance) {
		return Mono.fromRunnable(() -> {
			var name = instance.getRegistration().getName();
			var detail = (event instanceof InstanceStatusChangedEvent statusChangedEvent)
					? "is " + statusChangedEvent.getStatusInfo().getStatus() : event.getType();
			log.info("Instance {} ({}) {}", name, event.getInstance(), detail);
		});
	}

}
