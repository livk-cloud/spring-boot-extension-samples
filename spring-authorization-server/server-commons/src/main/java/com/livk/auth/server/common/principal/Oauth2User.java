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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.util.Assert;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author livk
 */
@Data
@Accessors(chain = true)
@JsonIgnoreProperties("password")
public class Oauth2User implements OAuth2AuthenticatedPrincipal, UserDetails, CredentialsContainer {

	@Serial
	private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

	/**
	 * 用户ID
	 */
	private Long id;

	/**
	 * 手机号
	 */
	private String mobile;

	private String password;

	@JsonProperty("name")
	private String username;

	private List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_USER");

	private boolean accountNonExpired = true;

	private boolean accountNonLocked = true;

	private boolean credentialsNonExpired = true;

	private boolean enabled = true;

	public Collection<GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	public String getPassword() {
		return this.password;
	}

	public String getUsername() {
		return this.username;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public boolean isAccountNonExpired() {
		return this.accountNonExpired;
	}

	public boolean isAccountNonLocked() {
		return this.accountNonLocked;
	}

	public boolean isCredentialsNonExpired() {
		return this.credentialsNonExpired;
	}

	public void eraseCredentials() {
		this.password = null;
	}

	private static SortedSet<GrantedAuthority> sortAuthorities(Collection<? extends GrantedAuthority> authorities) {
		Assert.notNull(authorities, "Cannot pass a null GrantedAuthority collection");
		SortedSet<GrantedAuthority> sortedAuthorities = new TreeSet<>(new AuthorityComparator());

		for (GrantedAuthority grantedAuthority : authorities) {
			Assert.notNull(grantedAuthority, "GrantedAuthority list cannot contain any null elements");
			sortedAuthorities.add(grantedAuthority);
		}

		return sortedAuthorities;
	}

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

	private static class AuthorityComparator implements Comparator<GrantedAuthority>, Serializable {

		private static final long serialVersionUID = 620L;

		public int compare(GrantedAuthority g1, GrantedAuthority g2) {
			if (g2.getAuthority() == null) {
				return -1;
			}
			else {
				return g1.getAuthority() == null ? 1 : g1.getAuthority().compareTo(g2.getAuthority());
			}
		}

	}

}
