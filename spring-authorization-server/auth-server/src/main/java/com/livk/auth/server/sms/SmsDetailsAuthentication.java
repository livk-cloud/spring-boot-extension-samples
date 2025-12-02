package com.livk.auth.server.sms;

import com.livk.auth.server.common.constant.SecurityConstants;
import com.livk.auth.server.common.DetailsAuthentication;
import com.livk.auth.server.common.exception.BadCaptchaException;
import com.livk.auth.server.common.principal.Oauth2User;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;

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
	public String type() {
		return SecurityConstants.SMS;
	}

	@Override
	public boolean verify(Oauth2User user, UsernamePasswordAuthenticationToken authentication) {
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
