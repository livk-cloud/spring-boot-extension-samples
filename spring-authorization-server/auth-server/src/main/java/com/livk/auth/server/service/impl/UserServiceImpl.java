/*
 * Copyright 2021-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * @author livk
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UsersMapper usersMapper;

	@Override
	public boolean support(String grantType) {
		return SecurityConstants.PASSWORD.equals(grantType);
	}

	@Override
	public Oauth2User loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = usersMapper.getByUsername(username);
		if (user != null) {
			return new Oauth2User(user.getId(), user.getUsername(), user.getPassword());
		}
		return null;
	}

}
