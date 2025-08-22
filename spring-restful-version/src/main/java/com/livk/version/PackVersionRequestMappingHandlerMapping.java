package com.livk.version;

import org.jspecify.annotations.NonNull;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * @author livk
 */
public class PackVersionRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

	@Override
	protected RequestMappingInfo getMappingForMethod(@NonNull Method method, @NonNull Class<?> handlerType) {
		Optional<RequestMappingInfo> mapping = Optional.ofNullable(super.getMappingForMethod(method, handlerType));
		Optional<ApiVersionCondition> typeOptional = Optional.ofNullable(handlerType.getAnnotation(ApiVersion.class))
			.map(ApiVersionCondition::new);

		Optional<ApiVersionCondition> methodOptional = Optional.ofNullable(method.getAnnotation(ApiVersion.class))
			.map(ApiVersionCondition::new);

		return typeOptional
			.map(typeApiVersion -> methodOptional.map(methodApiVersion -> methodApiVersion.combine(typeApiVersion))
				.orElse(typeApiVersion))
			.flatMap(apiVersionCondition -> mapping
				.map(mappingInfo -> mappingInfo.addCustomCondition(apiVersionCondition)))
			.orElse(null);
	}

}
