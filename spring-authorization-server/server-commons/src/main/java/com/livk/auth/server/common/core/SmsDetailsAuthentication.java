package com.livk.auth.server.common.core;

import com.livk.auth.server.common.constant.SecurityConstants;
import com.livk.auth.server.common.core.exception.BadCaptchaException;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.Objects;

/**
 * <p>
 * SmsDetailsAuthentication
 * </p>
 *
 * @author livk
 * @date 2025/11/26
 */
public class SmsDetailsAuthentication implements DetailsAuthentication {

	@Override
	public AuthorizationGrantType supportType() {
		return SecurityConstants.GRANT_TYPE_SMS;
	}

	@Override
	public boolean verify(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) {
		var mobile = authentication.getPrincipal().toString();
		var code = authentication.getCredentials().toString();
		return Objects.equals(code, "123456") && Objects.equals(mobile, "18664960000");
	}

	@Override
	public AuthenticationException error(MessageSourceAccessor messages) {
		return new BadCaptchaException(
				messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCaptcha", "Bad captcha"));
	}

}
