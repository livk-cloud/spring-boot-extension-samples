package com.livk.auth.server.common.resource;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrors;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * BearerTokenExtractor
 * </p>
 *
 * @author livk
 * @date 2025/11/25
 */
public class BearerTokenExtractor implements BearerTokenResolver {

	private static final Pattern authorizationPattern = Pattern.compile("^Bearer (?<token>[a-zA-Z0-9-:._~+/]+=*)$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public String resolve(HttpServletRequest request) {
		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (StringUtils.startsWithIgnoreCase(authorization, "bearer")) {
			Matcher matcher = authorizationPattern.matcher(authorization);
			if (!matcher.matches()) {
				BearerTokenError error = BearerTokenErrors.invalidToken("Bearer token is malformed");
				throw new OAuth2AuthenticationException(error);
			}
			return matcher.group("token");
		}
		return null;
	}

}
