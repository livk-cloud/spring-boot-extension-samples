package com.livk.auth.server.common.core;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

/**
 * <p>
 * DetailsAuthentication
 * </p>
 *
 * @author livk
 * @date 2025/11/26
 */
public interface DetailsAuthentication {

	AuthorizationGrantType supportType();

	default boolean support(String grantType) {
		return supportType().getValue().equals(grantType);
	}

	boolean verify(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication);

	AuthenticationException error(MessageSourceAccessor messages);

}
