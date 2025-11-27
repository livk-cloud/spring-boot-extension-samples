package com.livk.auth.server.service.impl;

import com.livk.auth.server.common.constant.SecurityConstants;
import com.livk.auth.server.common.principal.Oauth2User;
import com.livk.auth.server.domain.User;
import com.livk.auth.server.mapper.UsersMapper;
import com.livk.auth.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * <p>
 * EmailUserServiceImpl
 * </p>
 *
 * @author livk
 * @date 2025/11/26
 */
@Service
@RequiredArgsConstructor
public class SmsUserServiceImpl implements UserService {

	private final UsersMapper usersMapper;

	@Override
	public boolean support(String grantType) {
		return SecurityConstants.SMS.equals(grantType);
	}

	@Override
	public Oauth2User loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = usersMapper.getByMobile(username);
		if (user != null) {
			return new Oauth2User().setId(user.getId())
				.setUsername(user.getUsername())
				.setPassword(user.getPassword())
				.setMobile(user.getMobile());
		}
		return null;
	}

}
