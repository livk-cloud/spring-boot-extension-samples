package com.livk.version;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

/**
 * @author livk
 */
@Slf4j
@RequiredArgsConstructor
public class ApiVersionCondition implements RequestCondition<ApiVersionCondition> {

	private final ApiVersion apiVersion;

	@NonNull
	@Override
	public ApiVersionCondition combine(ApiVersionCondition other) {
		return this.apiVersion.value().compareTo(other.apiVersion.value()) >= 0 ? this : other;
	}

	@Override
	public ApiVersionCondition getMatchingCondition(HttpServletRequest request) {
		return apiVersion.value().equals(request.getHeader(apiVersion.header())) ? this : null;
	}

	@Override
	public int compareTo(ApiVersionCondition other, @NonNull HttpServletRequest request) {
		return this.apiVersion.value().compareTo(other.apiVersion.value());
	}

}
