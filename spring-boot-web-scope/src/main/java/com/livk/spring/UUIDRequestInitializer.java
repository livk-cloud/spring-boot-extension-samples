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

package com.livk.spring;

import com.livk.auto.service.annotation.SpringFactories;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.Serializable;

/**
 * @author livk
 */
@SpringFactories
public class UUIDRequestInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		applicationContext.addBeanFactoryPostProcessor(new UUIDBeanFactoryPostProcessor());
	}

	private static class UUIDBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
			beanFactory.registerResolvableDependency(UUIDRequest.class, new UUIDObjectFactory());
		}

	}

	/**
	 * <p>
	 * {@see AutowireUtils#resolveAutowiringValue(Object, Class)}
	 * 需要注册interface并且当前类实现{@link Serializable} 则会被spring代理
	 * </p>
	 */
	private static class UUIDObjectFactory implements ObjectFactory<UUIDRequest>, Serializable {

		@NonNull
		@Override
		public UUIDRequest getObject() throws BeansException {
			return UUIDConTextHolder::get;
		}

	}

}
