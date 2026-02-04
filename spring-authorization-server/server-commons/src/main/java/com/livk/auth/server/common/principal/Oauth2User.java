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

package com.livk.auth.server.common.principal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.io.Serial;
import java.util.Map;

/**
 * @author livk
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class Oauth2User extends User implements OAuth2AuthenticatedPrincipal {

	@Serial
	private static final long serialVersionUID = 3396186244563092877L;

	public Oauth2User(Long id, String username, String password, Oauth2UserType type) {
		super(username, password, AuthorityUtils.createAuthorityList("ROLE_USER"));
		this.id = id;
		this.type = type;
	}

	/**
	 * 用户ID
	 */
	private Long id;

	/**
	 * 手机号
	 */
	private Oauth2UserType type;

	/**
	 * Get the OAuth 2.0 token attributes
	 * @return the OAuth 2.0 token attributes
	 */
	@JsonIgnore
	@Override
	public Map<String, Object> getAttributes() {
		return Maps.newHashMap();
	}

	@JsonIgnore
	@Override
	public String getName() {
		return this.getUsername();
	}

	@Override
	public String toString() {
		return "Oauth2User{" + "id=" + id + ", username=" + super.getUsername() + ", type=" + type + '}';
	}

}
