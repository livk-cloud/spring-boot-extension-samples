package com.livk.version;

import com.livk.commons.util.HttpServletUtils;
import com.livk.commons.util.ObjectUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

/**
 * @author livk
 */
@WebFilter
@Component
public class RequestParamValidFilter implements Filter {

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = WebUtils.getNativeRequest(servletRequest, HttpServletRequest.class);
		Assert.notNull(request, "request must not be null");
		MultiValueMap<String, String> params = HttpServletUtils.params(request);
		if (params.containsKey("name")) {
			String name = params.getFirst("name");
			Assert.isTrue(ObjectUtils.isEmpty(name) || "Air Pod 3".equals(name), "name must be Air Pod 3");
		}
		chain.doFilter(servletRequest, servletResponse);
	}

}
