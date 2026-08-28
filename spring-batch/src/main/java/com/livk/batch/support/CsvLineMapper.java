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

package com.livk.batch.support;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.infrastructure.item.file.LineMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author livk
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CsvLineMapper<T> implements LineMapper<T> {

	private final Class<T> targetClass;

	private final String[] fields;

	private final String delimiter;

	public static <T> Builder<T> builder(Class<T> targetClass) {
		return new Builder<>(targetClass);
	}

	@NonNull
	@Override
	public T mapLine(@NonNull String line, int lineNumber) {
		var instance = BeanUtils.instantiateClass(targetClass);
		var fieldArray = line.split(delimiter);
		if (fieldArray.length != fields.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		BeanWrapper wrapper = new BeanWrapperImpl(instance);
		for (var i = 0; i < fields.length; i++) {
			wrapper.setPropertyValue(fields[i], fieldArray[i]);
		}
		return instance;
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Builder<T> {

		private final Class<T> targetClass;

		private String[] fields;

		private String delimiter;

		public Builder<T> fields(String... fields) {
			this.fields = fields;
			return this;
		}

		public Builder<T> delimiter(String delimiter) {
			this.delimiter = delimiter;
			return this;
		}

		public CsvLineMapper<T> build() {
			Assert.notNull(targetClass, "targetClass not null");
			Assert.notNull(fields, "fields not null");
			Assert.notNull(delimiter, "delimiter not null");
			return new CsvLineMapper<>(targetClass, fields, delimiter);
		}

	}

}
