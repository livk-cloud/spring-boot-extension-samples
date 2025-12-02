package com.livk.auth.server.common;

import com.livk.auth.server.common.principal.Oauth2User;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;

/**
 * <p>
 * DetailsAuthentication
 * </p>
 *
 * @author livk
 * @date 2025/11/26
 */
public interface DetailsAuthentication {

	String type();

	boolean verify(Oauth2User user, UsernamePasswordAuthenticationToken authentication);

	AuthenticationException error(MessageSourceAccessor messages);

}
