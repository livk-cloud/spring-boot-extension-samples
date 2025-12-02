package com.livk.auth.server.common.password;

import com.livk.auth.server.common.constant.SecurityConstants;
import com.livk.auth.server.common.DetailsAuthentication;
import com.livk.auth.server.common.principal.Oauth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

/**
 * <p>
 * PasswordDetailsAuthentication
 * </p>
 *
 * @author livk
 * @date 2025/11/26
 */
@Slf4j
@RequiredArgsConstructor
public class PasswordDetailsAuthentication implements DetailsAuthentication {

	private final PasswordEncoder passwordEncoder;

	@Override
	public String type() {
		return SecurityConstants.PASSWORD;
	}

	@Override
	public boolean verify(Oauth2User user, UsernamePasswordAuthenticationToken authentication) {
		var presentedPassword = authentication.getCredentials().toString();
		var encodedPassword = extractEncodedPassword(user.getPassword());
		return this.passwordEncoder.matches(presentedPassword, encodedPassword);
	}

	private String extractEncodedPassword(String prefixEncodedPassword) {
		var start = prefixEncodedPassword.indexOf('}');
		return prefixEncodedPassword.substring(start + SecurityConstants.DEFAULT_ID_SUFFIX.length());
	}

	@Override
	public AuthenticationException error(MessageSourceAccessor messages) {
		return new BadCredentialsException(
				messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
	}

}
